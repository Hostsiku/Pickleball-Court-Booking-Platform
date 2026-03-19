package com.pickleball.booking.service;

import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

public Venue createVenue(Venue venue, Long userId) {

    if (venue.getName() == null || venue.getName().isEmpty()
            || venue.getAddress() == null || venue.getAddress().isEmpty()
            || venue.getOpenTime() == null
            || venue.getCloseTime() == null
            || venue.getNoOfCourts() <= 0) {

        throw new RuntimeException("All venue fields are required");
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
}