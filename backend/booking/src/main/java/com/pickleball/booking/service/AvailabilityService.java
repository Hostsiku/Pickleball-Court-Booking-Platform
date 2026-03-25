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

    LocalDate selectedDate;

    // availability check code
    public Map<String, Object> checkAvailability(Long venueId, String date, Long userId) {

        // validation check
        if (venueId == null)
            throw new RuntimeException("VenueId required");
        if (date == null)
            throw new RuntimeException("Date required");

        try {
            selectedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format (yyyy-MM-dd required)");
        }

        // venue exist check
        Venue venue = venueRepository.findById(venueId).orElseThrow(() -> new RuntimeException("Venue not found"));

        List<Booking> bookings = bookingRepository.findByVenueIdAndBookingDate(venueId, date);

        Map<Integer, List<Booking>> bookingsByCourt = new HashMap<>();

        for (Booking b : bookings) {
            bookingsByCourt
                    .computeIfAbsent(b.getCourtId(), k -> new ArrayList<>())
                    .add(b);
        }

        // check its weekend
        boolean isWeekend = selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY
                || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY;

        // get date and check weter its today or not and past as well
        LocalDate today = LocalDate.now();
        boolean isToday = selectedDate.equals(today);
        boolean isPastDate = selectedDate.isBefore(today);

        int currentHour = LocalTime.now().getHour();

        double pricePerSlot = isWeekend ? venue.getWeekendRate() : venue.getWeekdayRate();

        int start = Integer.parseInt(venue.getOpenTime().split(":")[0]);
        int end = Integer.parseInt(venue.getCloseTime().split(":")[0]);

        List<Map<String, Object>> courts = new ArrayList<>();

        for (int court = 1; court <= venue.getNoOfCourts(); court++) {

            List<Map<String, Object>> slots = new ArrayList<>();

            // Get bookings only for this court
            List<Booking> courtBookings = bookingsByCourt.getOrDefault(court, Collections.emptyList());

            for (int i = start; i < end; i++) {

                int slotStart = i;
                int slotEnd = i + 1;

                String status = "Available";

                // Loop only relevant bookings
                for (Booking b : courtBookings) {

                    int bookedStart = Integer.parseInt(b.getStartTime().split(":")[0]);
                    int bookedEnd = Integer.parseInt(b.getEndTime().split(":")[0]);

                    boolean overlap = slotStart < bookedEnd && slotEnd > bookedStart;

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

                if (isPastDate) {
                    status = "Unavailable";
                } else if (isToday && slotEnd <= currentHour) {
                    status = "Unavailable";
                }

                Map<String, Object> slot = new HashMap<>();
                slot.put("time", slotStart + ":00 - " + slotEnd + ":00");
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