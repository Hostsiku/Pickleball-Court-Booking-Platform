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

    // 🔥 COMMON ROLE CHECK
    private void validateBooker(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"BOOKER".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Only BOOKER can access booking features");
        }
    }

    // 🔥 CLEAN EXPIRED CART ITEMS
    private void clearExpiredCart() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(CART_EXPIRY_MINUTES);
        List<Booking> expired =
                bookingRepository.findByStatusAndCreatedAtBefore("IN_CART", expiryTime);

        bookingRepository.deleteAll(expired);
    }

    // ✅ ADD TO CART
    public String addToCart(BookingRequest request, Long userId) {

        validateBooker(userId);
        clearExpiredCart(); // 🔥

        // 🔥 VALIDATION
        if (request.getVenueId() == null || request.getDate() == null
                || request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("All fields are required");
        }

        // 🔒 PREVENT DUPLICATE / CONFLICT
        Optional<Booking> existing = bookingRepository
                .findByVenueIdAndCourtIdAndBookingDateAndStartTime(
                        request.getVenueId(),
                        request.getCourtId(),
                        request.getDate(),
                        request.getStartTime()
                );

        if (existing.isPresent()) {
            Booking b = existing.get();

            // if still active cart or booked
            if ("BOOKED".equals(b.getStatus()) ||
                    ("IN_CART".equals(b.getStatus()) &&
                     b.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(CART_EXPIRY_MINUTES)))) {

                throw new RuntimeException("Slot already taken or in another user's cart");
            }
        }

        LocalTime start = LocalTime.parse(request.getStartTime());
        LocalTime end = LocalTime.parse(request.getEndTime());

        if (!end.equals(start.plusHours(1))) {
        throw new RuntimeException("Only 1-hour slot booking allowed");
        }

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

    // 🔥 GET CART (FIXED BUG)
    public Map<String, Object> getCart(Long userId) {

        validateBooker(userId);
        clearExpiredCart(); // 🔥

        List<Booking> cart = bookingRepository.findByUserIdAndStatus(userId, "IN_CART");

        if (cart.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        List<Map<String, Object>> items = new ArrayList<>();
        double total = 0;

        for (Booking b : cart) {

            Venue venue = venueRepository.findById(b.getVenueId()).orElseThrow();

            LocalDate date = LocalDate.parse(b.getBookingDate());
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;

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

    // 🔥 REMOVE
    public String removeFromCart(Long id, Long userId) {
        validateBooker(userId);
        bookingRepository.deleteById(id);
        return "Removed";
    }

    // 🔥 CHECKOUT
    @Transactional
public Map<String, Object> checkout(Long userId) {

    validateBooker(userId);
    clearExpiredCart();

    List<Booking> cart = bookingRepository.findByUserIdAndStatus(userId, "IN_CART");

    if (cart.isEmpty()) throw new RuntimeException("Cart empty");

    List<Map<String, Object>> invoice = new ArrayList<>();
    double total = 0;

    try {
        for (Booking b : cart) {

            // 🔥 IMPORTANT: RE-CHECK AVAILABILITY
            Optional<Booking> conflict = bookingRepository
                    .findByVenueIdAndCourtIdAndBookingDateAndStartTime(
                            b.getVenueId(),
                            b.getCourtId(),
                            b.getBookingDate(),
                            b.getStartTime()
                    );

            if (conflict.isPresent() &&
                    !conflict.get().getId().equals(b.getId()) &&
                    "BOOKED".equals(conflict.get().getStatus())) {

                // 🔥 PROPER CONFLICT MESSAGE
                throw new RuntimeException(
                        "Slot conflict: " + b.getStartTime() + "-" + b.getEndTime()
                );
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

    // 🔥 HISTORY
public List<Map<String, Object>> history(Long userId) {

    validateBooker(userId);

    List<Booking> bookings =
            bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "BOOKED");

    List<Map<String, Object>> result = new ArrayList<>();

    for (Booking b : bookings) {

        Venue venue = venueRepository.findById(b.getVenueId()).orElseThrow();

        LocalDate date = LocalDate.parse(b.getBookingDate());
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;

        double price = isWeekend ? venue.getWeekendRate() : venue.getWeekdayRate();

        Map<String, Object> item = new HashMap<>();
        item.put("court", "Court " + b.getCourtId()); // better format
        item.put("date", b.getBookingDate());
        item.put("time", b.getStartTime() + " - " + b.getEndTime());
        item.put("amountPaid", price); // 🔥 REQUIRED
        item.put("bookedAt", b.getCreatedAt()); // 🔥 timestamp

        result.add(item);
    }

    return result;
}

    // 🔥 RESCHEDULE
    @Transactional
    public String reschedule(Long bookingId, BookingRequest request, Long userId) {

        validateBooker(userId);
        clearExpiredCart();

        Booking oldBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!oldBooking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        LocalDateTime bookingTime = LocalDateTime.of(
                LocalDate.parse(oldBooking.getBookingDate()),
                LocalTime.parse(oldBooking.getStartTime())
        );

        if (bookingTime.minusHours(12).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot reschedule within 12 hours");
        }

        Optional<Booking> conflict = bookingRepository
                .findByVenueIdAndCourtIdAndBookingDateAndStartTime(
                        request.getVenueId(),
                        request.getCourtId(),
                        request.getDate(),
                        request.getStartTime()
                );

        if (conflict.isPresent() && "BOOKED".equals(conflict.get().getStatus())) {
            throw new RuntimeException("Slot already booked");
        }

        oldBooking.setBookingDate(request.getDate());
        oldBooking.setStartTime(request.getStartTime());
        oldBooking.setEndTime(request.getEndTime());

        bookingRepository.save(oldBooking);

        return "Rescheduled successfully";
    }

public List<Map<String, Object>> getOwnerBookings(Long venueId, Long userId, String date) {

    Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new RuntimeException("Venue not found"));

    if (!venue.getOwnerId().equals(userId)) {
        throw new RuntimeException("Unauthorized");
    }

    List<Booking> bookings =
            bookingRepository.findByVenueIdAndStatus(venueId, "BOOKED");

    // 🔥 FILTER BY DATE
    if (date != null) {
        bookings = bookings.stream()
                .filter(b -> b.getBookingDate().equals(date))
                .toList();
    }

    List<Map<String, Object>> result = new ArrayList<>();

    for (Booking b : bookings) {

        User user = userRepository.findById(b.getUserId()).orElseThrow();

        LocalDate d = LocalDate.parse(b.getBookingDate());
        boolean isWeekend = d.getDayOfWeek() == DayOfWeek.SATURDAY
                || d.getDayOfWeek() == DayOfWeek.SUNDAY;

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
}