package com.pickleball.booking.service;

import com.pickleball.booking.dto.BookingRequest;
import com.pickleball.booking.entity.Booking;
import com.pickleball.booking.entity.User;
import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.repository.BookingRepository;
import com.pickleball.booking.repository.UserRepository;
import com.pickleball.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    private static final int CART_EXPIRY_MINUTES = 10;

    // user roll validation
    private void validateBooker(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!"BOOKER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Only BOOKER can access booking features");
        }
    }

    // expire cart items after 10 min
    private void clearExpiredCart() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(CART_EXPIRY_MINUTES);
        List<Booking> expired = bookingRepository.findByStatusAndCreatedAtBefore("IN_CART", expiryTime);

        bookingRepository.deleteAll(expired);
    }

    // add to cart
    public String addToCart(BookingRequest request, Long userId) {

        validateBooker(userId);
        clearExpiredCart();

        // all feilds are required
        if (request.getVenueId() == null ||
                request.getCourtId() == 0 ||
                request.getDate() == null ||
                request.getStartTime() == null ||
                request.getEndTime() == null) {

            throw new RuntimeException("All fields are required");
        }

        // venue validation
        Venue venue = venueRepository.findById(request.getVenueId()).orElseThrow(() -> new RuntimeException("Venue not found"));

        // court validation
        if (request.getCourtId() < 1 || request.getCourtId() > venue.getNoOfCourts()) {
            throw new RuntimeException("Invalid courtId. Venue has only " + venue.getNoOfCourts() + " courts");
        }

        // date validation
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format (yyyy-MM-dd required)");
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Past date not allowed");
        }

        // time validation
        LocalTime start;
        LocalTime end;

        try {
            start = LocalTime.parse(request.getStartTime());
            end = LocalTime.parse(request.getEndTime());
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format (HH:mm required)");
        }

        // only one hr slot
        if (!end.equals(start.plusHours(1))) {
            throw new RuntimeException("Only 1-hour slot booking allowed");
        }

        // check within time
        int open = Integer.parseInt(venue.getOpenTime().split(":")[0]);
        int close = Integer.parseInt(venue.getCloseTime().split(":")[0]);

        if (start.getHour() < open || end.getHour() > close) {
            throw new RuntimeException("Slot outside venue working hours");
        }

        // check duplication and conflict
        Optional<Booking> existing = bookingRepository
                .findByVenueIdAndCourtIdAndBookingDateAndStartTime(
                        request.getVenueId(),
                        request.getCourtId(),
                        request.getDate(),
                        request.getStartTime());

        if (existing.isPresent()) {
            Booking b = existing.get();

            boolean activeCart = "IN_CART".equals(b.getStatus()) &&
                    b.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(CART_EXPIRY_MINUTES));

            if ("BOOKED".equals(b.getStatus()) || activeCart) {
                throw new RuntimeException("Slot already taken");
            }
        }

        // save booking
        Booking booking = new Booking();
        booking.setVenueId(request.getVenueId());
        booking.setCourtId(request.getCourtId());
        booking.setBookingDate(request.getDate());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setUserId(userId);
        booking.setStatus("IN_CART");
        booking.setCreatedAt(LocalDateTime.now());

        bookingRepository.save(booking);

        return "Added to cart";
    }

    // get cart items
    public Map<String, Object> getCart(Long userId) {

        validateBooker(userId);

        List<Booking> cart = bookingRepository.findByUserIdAndStatus(userId, "IN_CART");

        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        double total = 0;

        for (Booking b : cart) {

            Venue venue = venueRepository.findById(b.getVenueId()).orElseThrow();

            LocalDate date = LocalDate.parse(b.getBookingDate());
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;

            double price = isWeekend ? venue.getWeekendRate() : venue.getWeekdayRate();

            total += price;

            Map<String, Object> item = new HashMap<>();
            item.put("bookingId", b.getId());
            item.put("courtId", b.getCourtId());
            item.put("date", b.getBookingDate());
            item.put("time", b.getStartTime() + " - " + b.getEndTime());
            item.put("price", price);

            items.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("totalAmount", total);
        response.put("items", items);

        return response;
    }

    // remove cart items
    public String removeFromCart(Long id, Long userId) {

        validateBooker(userId);

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Check ownership
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Only allow cart removal
        if (!"IN_CART".equals(booking.getStatus())) {
            throw new RuntimeException("Only cart items can be removed");
        }

        bookingRepository.delete(booking);

        return "Removed from cart successfully";
    }

    // checkout option code
    @Transactional
    public Map<String, Object> checkout(Long userId) {

        validateBooker(userId);
        clearExpiredCart();

        List<Booking> cart = bookingRepository.findByUserIdAndStatus(userId, "IN_CART");

        if (cart.isEmpty())
            throw new RuntimeException("Cart empty");

        List<Map<String, Object>> invoice = new ArrayList<>();
        double total = 0;

        try {
            for (Booking b : cart) {

                // re check available
                Optional<Booking> conflict = bookingRepository
                        .findByVenueIdAndCourtIdAndBookingDateAndStartTime(
                                b.getVenueId(),
                                b.getCourtId(),
                                b.getBookingDate(),
                                b.getStartTime());

                if (conflict.isPresent() && !conflict.get().getId().equals(b.getId())
                        && "BOOKED".equals(conflict.get().getStatus())) {

                    throw new RuntimeException("Slot conflict: " + b.getStartTime() + "-" + b.getEndTime());
                }

                Venue venue = venueRepository.findById(b.getVenueId()).orElseThrow();

                LocalDate date = LocalDate.parse(b.getBookingDate());
                boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                        || date.getDayOfWeek() == DayOfWeek.SUNDAY;

                double price = isWeekend ? venue.getWeekendRate() : venue.getWeekdayRate();

                total += price;

                b.setStatus("BOOKED");
                bookingRepository.save(b);

                Map<String, Object> item = new HashMap<>();
                item.put("courtId", b.getCourtId());
                item.put("time", b.getStartTime() + "-" + b.getEndTime());
                item.put("price", price);

                invoice.add(item);
            }

        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Slot conflict detected");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Booking successful");
        res.put("totalAmount", total);
        res.put("bookings", invoice);

        return res;
    }

    // booking history
    public List<Map<String, Object>> history(Long userId) {

        validateBooker(userId);

        List<Booking> bookings = bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "BOOKED");

        List<Map<String, Object>> result = new ArrayList<>();

        for (Booking b : bookings) {

            Venue venue = venueRepository.findById(b.getVenueId()).orElseThrow();

            LocalDate date = LocalDate.parse(b.getBookingDate());
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;

            double price = isWeekend ? venue.getWeekendRate() : venue.getWeekdayRate();

            Map<String, Object> item = new HashMap<>();
            item.put("court", "Court " + b.getCourtId());
            item.put("date", b.getBookingDate());
            item.put("time", b.getStartTime() + " - " + b.getEndTime());
            item.put("amountPaid", price);
            item.put("bookedAt", b.getCreatedAt());

            result.add(item);
        }

        return result;
    }

    // reschedule booking code
    @Transactional
    public String reschedule(Long bookingId, BookingRequest request, Long userId) {

        validateBooker(userId);
        clearExpiredCart();

        Booking oldBooking = bookingRepository.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!oldBooking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // validate venue exist
        Venue venue = venueRepository.findById(request.getVenueId()).orElseThrow(() -> new RuntimeException("Venue not found"));

        if (venue.getId() == null) {
            throw new RuntimeException("Invalid venue");
        }

        // validate code exist in venue
        if (request.getCourtId() < 1 || request.getCourtId() > venue.getNoOfCourts()) {
            throw new RuntimeException("Invalid courtId. Venue has only " + venue.getNoOfCourts() + " courts");
        }

        // allow only same venue
        if (!oldBooking.getVenueId().equals(request.getVenueId())) {
            throw new RuntimeException("Reschedule only allowed within same venue");
        }

        // date validation
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format (yyyy-MM-dd required)");
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Past date not allowed");
        }

        // time validation
        LocalTime start;
        LocalTime end;

        try {
            start = LocalTime.parse(request.getStartTime());
            end = LocalTime.parse(request.getEndTime());
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format (HH:mm required)");
        }

        if (!end.equals(start.plusHours(1))) {
            throw new RuntimeException("Only 1-hour slot booking allowed");
        }

        // check within times
        int open = Integer.parseInt(venue.getOpenTime().split(":")[0]);
        int close = Integer.parseInt(venue.getCloseTime().split(":")[0]);

        if (start.getHour() < open || end.getHour() > close) {
            throw new RuntimeException("Slot outside venue working hours");
        }

        LocalDateTime bookingTime = LocalDateTime.of(
                LocalDate.parse(oldBooking.getBookingDate()),
                LocalTime.parse(oldBooking.getStartTime()));

        
        // check within 12 hrs
        if (bookingTime.minusHours(12).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot reschedule within 12 hours");
        }

        // check duplication and conflict
        Optional<Booking> conflict = bookingRepository
                .findByVenueIdAndCourtIdAndBookingDateAndStartTime(
                        request.getVenueId(),
                        request.getCourtId(),
                        request.getDate(),
                        request.getStartTime());

        if (conflict.isPresent() && "BOOKED".equals(conflict.get().getStatus())) {
            throw new RuntimeException("Slot already booked");
        }

        oldBooking.setBookingDate(request.getDate());
        oldBooking.setStartTime(request.getStartTime());
        oldBooking.setEndTime(request.getEndTime());

        bookingRepository.save(oldBooking);

        return "Rescheduled successfully";
    }

    // bookings at venues
    public List<Map<String, Object>> getOwnerBookings(Long venueId, Long userId, String date) {

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        if (!venue.getOwnerId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        List<Booking> bookings = bookingRepository.findByVenueIdAndStatus(venueId, "BOOKED");

        // filter by date
        if (date != null) {
            bookings = bookings.stream().filter(b -> b.getBookingDate().equals(date)).toList();
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Booking b : bookings) {

            User user = userRepository.findById(b.getUserId()).orElseThrow();

            LocalDate d = LocalDate.parse(b.getBookingDate());
            boolean isWeekend = d.getDayOfWeek() == DayOfWeek.SATURDAY || d.getDayOfWeek() == DayOfWeek.SUNDAY;

            Venue v = venueRepository.findById(b.getVenueId()).orElseThrow();

            double price = isWeekend ? v.getWeekendRate() : v.getWeekdayRate();

            Map<String, Object> item = new HashMap<>();
            item.put("court", "Court " + b.getCourtId());
            item.put("date", b.getBookingDate());
            item.put("time", b.getStartTime() + " - " + b.getEndTime());
            item.put("bookerName", user.getName());
            item.put("amount", price);

            result.add(item);
        }

        return result;
    }

    // cancel booking
    @Transactional
    public String cancelBooking(Long bookingId, Long userId) {

        validateBooker(userId);

        // booking exist
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Only owner can cancel
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Only BOOKED bookings can be cancelled
        if (!"BOOKED".equals(booking.getStatus())) {
            throw new RuntimeException("Only confirmed bookings can be cancelled");
        }

        // 12 hour restriction
        LocalDateTime bookingTime = LocalDateTime.of(
                LocalDate.parse(booking.getBookingDate()),
                LocalTime.parse(booking.getStartTime()));

        if (bookingTime.minusHours(12).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot cancel within 12 hours");
        }

        bookingRepository.delete(booking);

        return "Booking cancelled successfully";
    }
}