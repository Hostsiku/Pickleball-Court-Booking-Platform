package com.pickleball.booking.service;

import com.pickleball.booking.entity.Booking;
import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.repository.BookingRepository;
import com.pickleball.booking.repository.VenueRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class AvailabilityService {

    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;

    public AvailabilityService(VenueRepository venueRepository, BookingRepository bookingRepository) {
        this.venueRepository = venueRepository;
        this.bookingRepository = bookingRepository;
    }


    // add to cart
    public Map<String, Object> checkAvailability(Long venueId, String date, Long userId) {

        if (venueId == null)
            throw new RuntimeException("VenueId required");
        if (date == null)
            throw new RuntimeException("Date required");

        LocalDate selectedDate;

        try {
            selectedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format (yyyy-MM-dd required)");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new RuntimeException("Venue not found"));

        List<Booking> bookings = bookingRepository
                .findByVenueIdAndBookingDate(venueId, date);

        Map<Integer, List<Booking>> bookingsByCourt = new HashMap<>();

        for (Booking b : bookings) {
            bookingsByCourt
                    .computeIfAbsent(b.getCourtId(), k -> new ArrayList<>())
                    .add(b);
        }

        boolean isWeekend = selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY;

        LocalDate today = LocalDate.now();
        boolean isToday = selectedDate.equals(today);
        boolean isPastDate = selectedDate.isBefore(today);

        int currentHour = LocalTime.now().getHour();

        double pricePerSlot = isWeekend
                ? venue.getWeekendRate()
                : venue.getWeekdayRate();

        int start = Integer.parseInt(venue.getOpenTime().split(":")[0]);
        int end = Integer.parseInt(venue.getCloseTime().split(":")[0]);

        //  SLOT GENERATION LOGIC
        List<Integer> hours = new ArrayList<>();

        if (start == end) {
            for (int i = 0; i < 24; i++) {
                hours.add(i);
            }
        }

        else if (start < end) {
            for (int i = start; i < end; i++) {
                hours.add(i);
            }
        }

        else {
            for (int i = start; i < 24; i++) {
                hours.add(i);
            }
            for (int i = 0; i < end; i++) {
                hours.add(i);
            }
        }

        List<Map<String, Object>> courts = new ArrayList<>();

        for (int court = 1; court <= venue.getNoOfCourts(); court++) {

            List<Map<String, Object>> slots = new ArrayList<>();

            List<Booking> courtBookings = bookingsByCourt.getOrDefault(court, Collections.emptyList());

            for (int i : hours) {

                int slotStart = i;
                int slotEnd = (i + 1) % 24; 

                String status = "Available";

                for (Booking b : courtBookings) {

                    int bookedStart = Integer.parseInt(b.getStartTime().split(":")[0]);
                    int bookedEnd = Integer.parseInt(b.getEndTime().split(":")[0]);

                    boolean overlap;

                    if (bookedStart < bookedEnd) {
                        overlap = slotStart < bookedEnd && slotEnd > bookedStart;
                    } else {
                        overlap = (slotStart >= bookedStart || slotEnd <= bookedEnd);
                    }

                    if (overlap) {

                        if ("IN_CART".equals(b.getStatus())) {

                            boolean active = b.getCreatedAt()
                                    .isAfter(LocalDateTime.now().minusMinutes(10));

                            if (active) {
                                if (userId != null && b.getUserId().equals(userId)) {
                                    status = "IN_CART";
                                    break;
                                } else {
                                    status = "BOOKED";
                                }
                            }
                        }

                        else if ("BOOKED".equals(b.getStatus())) {
                            status = "BOOKED";
                            break;
                        }
                    }
                }

                // DATE VALIDATION
                if (isPastDate) {
                    status = "Unavailable";
                } else if (isToday) {

                    LocalTime now = LocalTime.now();

                    LocalTime slotStartTime = LocalTime.of(slotStart, 0);
                    LocalTime slotEndTime = LocalTime.of(slotEnd, 0);

                    if (slotEnd == 0) {
                        slotEndTime = LocalTime.of(23, 59);
                    }

                    // if slot already finished
                    if (!slotEndTime.isAfter(now)) {
                        status = "Unavailable";
                    }
                }

                Map<String, Object> slot = new HashMap<>();

                slot.put("time",
                        String.format("%02d:00 - %02d:00", slotStart, slotEnd));

                slot.put("status", status);
                slot.put("price", pricePerSlot);

                slots.add(slot);
            }

            Map<String, Object> courtMap = new HashMap<>();
            courtMap.put("courtId", court);
            courtMap.put("slots", slots);

            courts.add(courtMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("venueId", venueId);
        response.put("date", date);
        response.put("courts", courts);

        return response;
    }
}