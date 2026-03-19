package com.pickleball.booking.service;

import com.pickleball.booking.entity.Booking;
import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.repository.VenueRepository;
import com.pickleball.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;

public Venue createVenue(Venue venue, Long userId) {

    if (venue.getName() == null || venue.getName().isEmpty()
            || venue.getAddress() == null || venue.getAddress().isEmpty()
            || venue.getOpenTime() == null
            || venue.getCloseTime() == null
            || venue.getNoOfCourts() <= 0) {

        throw new RuntimeException("All venue fields are required");
    }

if (venue.getPhotos() != null && venue.getPhotos().size() > 5) {
    throw new RuntimeException("Maximum 5 photos allowed");
}

    venue.setOwnerId(userId);
    return venueRepository.save(venue);
}

    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    public Venue getVenueById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found"));
    }

    public Venue updateVenue(Long id, Venue updatedVenue, Long userId) {
        Venue venue = getVenueById(id);

        if (!venue.getOwnerId().equals(userId)) {
            throw new RuntimeException("Not your venue");
        }

if (updatedVenue.getPhotos() != null && updatedVenue.getPhotos().size() > 5) {
    throw new RuntimeException("Maximum 5 photos allowed");
}

venue.setPhotos(updatedVenue.getPhotos());

        venue.setName(updatedVenue.getName());
        venue.setNoOfCourts(updatedVenue.getNoOfCourts());
        venue.setOpenTime(updatedVenue.getOpenTime());
        venue.setCloseTime(updatedVenue.getCloseTime());
        venue.setWeekendRate(updatedVenue.getWeekendRate());
        venue.setWeekdayRate(updatedVenue.getWeekdayRate());
        venue.setPhoneNo(updatedVenue.getPhoneNo());
        venue.setEmail(updatedVenue.getEmail());
        venue.setDescription(updatedVenue.getDescription());
        venue.setAddress(updatedVenue.getAddress());

        return venueRepository.save(venue);
    }

    public void deleteVenue(Long id, Long userId) {
        Venue venue = getVenueById(id);

        if (!venue.getOwnerId().equals(userId)) {
            throw new RuntimeException("Not your venue");
        }

        venueRepository.delete(venue);
    }

    // for market place
public List<Map<String, Object>> getMarketplaceVenues() {

    List<Venue> venues = venueRepository.findAll();

    List<Map<String, Object>> result = new ArrayList<>();

    for (Venue v : venues) {

        Map<String, Object> item = new HashMap<>();
        item.put("id", v.getId());
        item.put("name", v.getName());
        item.put("location", v.getAddress());
        item.put("courts", v.getNoOfCourts());

        // 🔥 starting price = min of weekday/weekend
        int startingPrice = Math.min(v.getWeekdayRate(), v.getWeekendRate());
        item.put("startingPrice", startingPrice);

        // 🔥 thumbnail (first photo)
        if (v.getPhotos() != null && !v.getPhotos().isEmpty()) {
            item.put("photo", v.getPhotos().get(0));
        } else {
            item.put("photo", null);
        }

        result.add(item);
    }

    return result;
}

// filters for booker with date and time
public List<Map<String, Object>> filterVenues(String date, String time) {

    List<Venue> venues = venueRepository.findAll();
    List<Map<String, Object>> result = new ArrayList<>();

    for (Venue v : venues) {

        List<Booking> bookings =
                bookingRepository.findByVenueIdAndBookingDate(v.getId(), date);

        boolean available = false;

        for (int court = 1; court <= v.getNoOfCourts(); court++) {

            boolean booked = false;

            for (Booking b : bookings) {

                boolean isActiveCart =
                        "IN_CART".equals(b.getStatus()) &&
                        b.getCreatedAt().isAfter(
                                java.time.LocalDateTime.now().minusMinutes(10)
                        );

                if (b.getCourtId() == court &&
                        b.getStartTime().equals(time) &&
                        ("BOOKED".equals(b.getStatus()) || isActiveCart)) {

                    booked = true;
                    break;
                }
            }

            if (!booked) {
                available = true;
                break;
            }
        }

        if (available) {

            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getId());
            item.put("name", v.getName());
            item.put("location", v.getAddress());
            item.put("courts", v.getNoOfCourts());

            int price = Math.min(v.getWeekdayRate(), v.getWeekendRate());
            item.put("startingPrice", price);

            // 🔥 FIX: include photo
            if (v.getPhotos() != null && !v.getPhotos().isEmpty()) {
                item.put("photo", v.getPhotos().get(0));
            } else {
                item.put("photo", null);
            }

            result.add(item);
        }
    }

    return result;
}
}