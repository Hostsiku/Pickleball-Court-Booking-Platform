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

    public AvailabilityService(VenueRepository venueRepository,
                               BookingRepository bookingRepository) {
        this.venueRepository = venueRepository;
        this.bookingRepository = bookingRepository;
    }

public Map<String, Object> checkAvailability(Long venueId, String date, Long userId) {

    if (venueId == null) throw new RuntimeException("VenueId required");
    if (date == null) throw new RuntimeException("Date required");

    Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new RuntimeException("Venue not found"));

    List<Booking> bookings =
            bookingRepository.findByVenueIdAndBookingDate(venueId, date);

    LocalDate selectedDate = LocalDate.parse(date);
    boolean isWeekend = selectedDate.getDayOfWeek() == DayOfWeek.SATURDAY
            || selectedDate.getDayOfWeek() == DayOfWeek.SUNDAY;

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

        for (int i = start; i < end; i++) {

            int slotStart = i;
            int slotEnd = i + 1;

            String status = "Available";

            for (Booking b : bookings) {

                if (b.getCourtId() != court) continue;

                int bookedStart = Integer.parseInt(b.getStartTime().split(":")[0]);
                int bookedEnd = Integer.parseInt(b.getEndTime().split(":")[0]);

                boolean overlap = slotStart < bookedEnd && slotEnd > bookedStart;

                if (overlap) {

                    if ("BOOKED".equals(b.getStatus())) {
                        status = "Booked";
                    }

                    else if ("IN_CART".equals(b.getStatus())) {

                        boolean active =
                                b.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(10));

                        if (active) {

                            if (b.getUserId().equals(userId)) {
                                status = "In Cart";
                            } else {
                                status = "Booked";
                            }
                        }
                    }

                    break;
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