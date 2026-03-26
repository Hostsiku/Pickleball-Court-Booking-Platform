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
import org.springframework.transaction.annotation.Isolation;
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

    // expire cart items after 10 min
    private void clearExpiredCart() {
        LocalDateTime expiryTime = LocalDateTime.now().minusMinutes(CART_EXPIRY_MINUTES);
        List<Booking> expired = bookingRepository.findByStatusAndCreatedAtBefore("IN_CART", expiryTime);

        bookingRepository.deleteAll(expired);
    }

    // add to cart
    public String addToCart(BookingRequest request, Long userId) {

        clearExpiredCart();

        // ✅ VALIDATION: required fields
        if (request.getVenueId() == null ||
                request.getCourtId() == 0 ||
                request.getDate() == null ||
                request.getStartTime() == null ||
                request.getEndTime() == null) {

            throw new RuntimeException("All fields are required");
        }

        // ✅ TRIM INPUT (VERY IMPORTANT)
        String startTimeStr = request.getStartTime().trim();
        String endTimeStr = request.getEndTime().trim();

        // ✅ VENUE VALIDATION
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        // ✅ COURT VALIDATION
        if (request.getCourtId() < 1 || request.getCourtId() > venue.getNoOfCourts()) {
            throw new RuntimeException("Invalid courtId. Venue has only " + venue.getNoOfCourts() + " courts");
        }

        // ✅ DATE VALIDATION
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format (yyyy-MM-dd required)");
        }

        if (parsedDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Past date not allowed");
        }

        // ✅ TIME PARSING
        LocalTime start;
        LocalTime end;

        try {
            start = LocalTime.parse(startTimeStr);
            end = LocalTime.parse(endTimeStr);
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format (HH:mm required)");
        }

        // ✅ VALIDATE 1-HOUR SLOT
        if (!end.equals(start.plusHours(1))) {
            throw new RuntimeException("Only 1-hour slot booking allowed");
        }

        // ✅ PARSE VENUE TIMES PROPERLY
        LocalTime openTime = LocalTime.parse(venue.getOpenTime().trim());
        LocalTime closeTime = LocalTime.parse(venue.getCloseTime().trim());

        // ✅ HANDLE ALL CASES: NORMAL + OVERNIGHT + 24x7

        boolean isValidTime;

        // 24x7 case
        if (openTime.equals(closeTime)) {
            isValidTime = true;
        }

        // Normal case (07:00 → 22:00)
        else if (openTime.isBefore(closeTime)) {
            isValidTime = !start.isBefore(openTime) &&
                    !end.isAfter(closeTime);
        }

        // Overnight case (07:00 → 01:00)
        else {
            isValidTime = (!start.isBefore(openTime) || !end.isAfter(closeTime));
        }

        if (!isValidTime) {
            throw new RuntimeException("Slot must be within " + openTime + " to " + closeTime);
        }

        // ✅ CHECK DUPLICATE / CONFLICT
        Optional<Booking> existing = bookingRepository
                .findByVenueIdAndCourtIdAndBookingDateAndStartTime(
                        request.getVenueId(),
                        request.getCourtId(),
                        request.getDate(),
                        startTimeStr);

        if (existing.isPresent()) {

            Booking b = existing.get();

            boolean activeCart = "IN_CART".equals(b.getStatus()) &&
                    b.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(CART_EXPIRY_MINUTES));

            if ("BOOKED".equals(b.getStatus()) || activeCart) {
                throw new RuntimeException("Slot already taken");
            }
        }

        // ✅ SAVE BOOKING
        Booking booking = new Booking();
        booking.setVenueId(request.getVenueId());
        booking.setCourtId(request.getCourtId());
        booking.setBookingDate(request.getDate());
        booking.setStartTime(startTimeStr);
        booking.setEndTime(endTimeStr);
        booking.setUserId(userId);
        booking.setStatus("IN_CART");
        booking.setCreatedAt(LocalDateTime.now());

        try {
            bookingRepository.save(booking);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Slot already taken");
        }

        return "Added to cart";
    }

    // get cart items
    public Map<String, Object> getCart(Long userId) {

        clearExpiredCart();

        List<Booking> cart = bookingRepository.findByUserIdAndStatus(userId, "IN_CART");

        List<Map<String, Object>> items = new ArrayList<>();
        double total = 0;

        // If cart is empty return empty response
        if (cart.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("totalAmount", 0);
            response.put("items", items);
            return response;
        }

        for (Booking b : cart) {

            Venue venue = venueRepository.findById(b.getVenueId()).orElseThrow();

            // 🧠 DATE LOGIC
            LocalDate date = LocalDate.parse(b.getBookingDate());
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    date.getDayOfWeek() == DayOfWeek.SUNDAY;

            double price = isWeekend ? venue.getWeekendRate() : venue.getWeekdayRate();

            total += price;

            // 🔥 FORMAT DATE (Optional but nice UX)
            String formattedDate = date.toString(); // or use DateTimeFormatter

            // 🔥 COURT NAME (if you don’t have table, fallback)
            String courtName = "Court " + b.getCourtId();

            Map<String, Object> item = new HashMap<>();

            item.put("bookingId", b.getId());

            // ✅ NEW FIELDS (IMPORTANT)
            item.put("venueName", venue.getName());
            item.put("courtName", courtName);

            item.put("courtId", b.getCourtId());
            item.put("date", formattedDate);
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

        Booking booking = bookingRepository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));

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
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Map<String, Object> checkout(Long userId) {

        // validateBooker(userId);
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
            throw new RuntimeException(
                    "One or more slots were already booked by another user. Please refresh and try again.");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("message", "Booking successful");
        res.put("totalAmount", total);
        res.put("bookings", invoice);

        return res;
    }

    // booking history
    public List<Map<String, Object>> history(Long userId) {

        // 🔥 GET ALL BOOKINGS (not just BOOKED)
        List<Booking> bookings = bookingRepository
                .findByUserIdOrderByCreatedAtDesc(userId);

        List<Map<String, Object>> result = new ArrayList<>();

        for (Booking b : bookings) {

            Venue venue = venueRepository.findById(b.getVenueId()).orElseThrow();

            LocalDate date = LocalDate.parse(b.getBookingDate());
            LocalTime time = LocalTime.parse(b.getStartTime());
            LocalDateTime bookingDateTime = LocalDateTime.of(date, time);

            // ✅ STATUS LOGIC
            String status;

            if ("CANCELLED".equals(b.getStatus())) {
                status = "CANCELLED";
            } else if (bookingDateTime.isAfter(LocalDateTime.now())) {
                status = "UPCOMING";
            } else {
                status = "PAST";
            }

            // ✅ 12 HOUR RULE (for reschedule/cancel)
            boolean canModify = bookingDateTime.minusHours(12)
                    .isAfter(LocalDateTime.now());

            // ✅ PRICE CALCULATION
            boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                    || date.getDayOfWeek() == DayOfWeek.SUNDAY;

            double price = isWeekend
                    ? venue.getWeekendRate()
                    : venue.getWeekdayRate();

            // ✅ RESPONSE MAP
            Map<String, Object> item = new HashMap<>();

            item.put("bookingId", b.getId()); // IMPORTANT
            item.put("venueName", venue.getName());
            item.put("court", "Court " + b.getCourtId());
            item.put("date", b.getBookingDate());
            item.put("time", b.getStartTime() + " - " + b.getEndTime());
            item.put("amountPaid", price);
            item.put("status", status);
            item.put("canModify", canModify);
            item.put("bookedAt", b.getCreatedAt());
            item.put("venueId", b.getVenueId());

            result.add(item);
        }

        return result;
    }

    // reschedule booking code
    @Transactional
    public String reschedule(Long bookingId, BookingRequest request, Long userId) {

        // 🔍 Fetch booking
        Booking oldBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 🔐 Ownership check
        if (!oldBooking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // ✅ Only BOOKED can reschedule
        if (!"BOOKED".equals(oldBooking.getStatus())) {
            throw new RuntimeException("Only confirmed bookings can be rescheduled");
        }

        // 🏟️ Validate venue
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        // 🔒 Same venue restriction
        if (!oldBooking.getVenueId().equals(request.getVenueId())) {
            throw new RuntimeException("Reschedule allowed only within same venue");
        }

        // 🏸 Court validation
        if (request.getCourtId() < 1 || request.getCourtId() > venue.getNoOfCourts()) {
            throw new RuntimeException("Invalid courtId. Venue has only " + venue.getNoOfCourts() + " courts");
        }

        // 📅 Date validation
        LocalDate date;
        try {
            date = LocalDate.parse(request.getDate());
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format (yyyy-MM-dd)");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new RuntimeException("Past date not allowed");
        }

        // ⏰ Time validation
        LocalTime start;
        LocalTime end;

        try {
            start = LocalTime.parse(request.getStartTime());
            end = LocalTime.parse(request.getEndTime());
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format (HH:mm)");
        }

        if (!end.equals(start.plusHours(1))) {
            throw new RuntimeException("Only 1-hour slots allowed");
        }

        // 🕐 Venue working hours (FIXED)
        LocalTime openTime = LocalTime.parse(venue.getOpenTime());
        LocalTime closeTime = LocalTime.parse(venue.getCloseTime());

        // CASE 1: Normal timing (07:00 → 22:00)
        if (closeTime.isAfter(openTime)) {

            if (start.isBefore(openTime) || end.isAfter(closeTime)) {
                throw new RuntimeException("Slot outside venue working hours");
            }

        }
        // CASE 2: Overnight timing (07:00 → 01:00)
        else {

            boolean valid =
                    // same day (after opening)
                    (!start.isBefore(openTime)) ||

                    // after midnight (before closing)
                            (!end.isAfter(closeTime));

            if (!valid) {
                throw new RuntimeException("Slot outside venue working hours");
            }
        }

        // ⛔ 12-hour restriction
        LocalDateTime oldBookingTime = LocalDateTime.of(
                LocalDate.parse(oldBooking.getBookingDate()),
                LocalTime.parse(oldBooking.getStartTime()));

        if (oldBookingTime.minusHours(12).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot reschedule within 12 hours");
        }

        // ❗ Conflict check (IMPORTANT FIX)
        boolean exists = bookingRepository
                .existsByVenueIdAndCourtIdAndBookingDateAndStartTimeAndStatus(
                        request.getVenueId(),
                        request.getCourtId(),
                        request.getDate(),
                        request.getStartTime(),
                        "BOOKED");

        if (exists &&
                !(oldBooking.getBookingDate().equals(request.getDate())
                        && oldBooking.getStartTime().equals(request.getStartTime())
                        && oldBooking.getCourtId() == request.getCourtId())) {

            throw new RuntimeException("Slot already booked");
        }

        // ✅ UPDATE BOOKING
        oldBooking.setBookingDate(request.getDate());
        oldBooking.setStartTime(request.getStartTime());
        oldBooking.setEndTime(request.getEndTime());
        oldBooking.setCourtId(request.getCourtId());

        bookingRepository.save(oldBooking);

        return "Rescheduled successfully";
    }

    // get bookings
    public Booking getBookingById(Long id, Long userId) {

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        return booking;
    }

    // cancel booking
    @Transactional
    public String cancelBooking(Long bookingId, Long userId) {

        // 🔍 Check booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // 🔒 Ownership check
        if (!booking.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // ❌ Already cancelled
        if ("CANCELLED".equals(booking.getStatus())) {
            throw new RuntimeException("Booking already cancelled");
        }

        // ❌ Only BOOKED bookings can be cancelled
        if (!"BOOKED".equals(booking.getStatus())) {
            throw new RuntimeException("Only confirmed bookings can be cancelled");
        }

        // ⏰ Booking time
        LocalDateTime bookingTime = LocalDateTime.of(
                LocalDate.parse(booking.getBookingDate()),
                LocalTime.parse(booking.getStartTime()));

        // ⛔ 12-hour restriction
        if (bookingTime.minusHours(12).isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Cannot cancel within 12 hours of booking time");
        }

        // ✅ UPDATE STATUS INSTEAD OF DELETE
        booking.setStatus("CANCELLED");

        bookingRepository.save(booking);

        return "Booking cancelled successfully";
    }

    // bookings at venues for owner
    public List<Map<String, Object>> getOwnerBookings(Long venueId, Long userId, String date) {

        Venue venue = venueRepository.findById(venueId).orElseThrow(() -> new RuntimeException("Venue not found"));

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
}