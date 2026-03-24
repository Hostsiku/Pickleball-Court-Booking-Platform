package com.pickleball.booking.service;

import com.pickleball.booking.entity.Booking;
import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.entity.VenuePhoto;
import com.pickleball.booking.repository.VenueRepository;
import com.pickleball.booking.repository.BookingRepository;
import com.pickleball.booking.repository.VenuePhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;
    private final BookingRepository bookingRepository;
    private final VenuePhotoRepository venuePhotoRepository;

    @Value("${app.base-url}")
    private String baseUrl;

    // create venue code
    public Venue createVenue(Venue venue, Long userId) {

        if (venue.getName() == null || venue.getName().isEmpty()
                || venue.getAddress() == null || venue.getAddress().isEmpty()
                || venue.getOpenTime() == null
                || venue.getCloseTime() == null
                || venue.getPhoneNo() == null
                || venue.getEmail() == null
                || venue.getDescription() == null
                || venue.getNoOfCourts() <= 0) {

            throw new RuntimeException("All venue fields are required");
        }

        // time validation
        LocalTime open;
        LocalTime close;

        try {
            open = LocalTime.parse(venue.getOpenTime());
            close = LocalTime.parse(venue.getCloseTime());
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format (HH:mm required)");
        }

        if (!open.isBefore(close)) {
            throw new RuntimeException("Open time must be before close time");
        }

        venue.setOwnerId(userId);
        return venueRepository.save(venue);
    }

    // get all venues
    public List<Venue> getAllVenues() {
        return venueRepository.findAll();
    }

    // get venues by id
    public Venue getVenueById(Long id) {
        return venueRepository.findById(id).orElseThrow(() -> new RuntimeException("Venue not found"));
    }


    // get venue details by id code
    public Map<String, Object> getVenueDetails(Long id) {

    Venue v = venueRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venue not found"));

    Map<String, Object> result = new HashMap<>();

    result.put("id", v.getId());
    result.put("name", v.getName());
    result.put("location", v.getAddress());
    result.put("description", v.getDescription());
    result.put("courts", v.getNoOfCourts());

    result.put("openTime", v.getOpenTime());
    result.put("closeTime", v.getCloseTime());

    result.put("weekdayPrice", v.getWeekdayRate());
    result.put("weekendPrice", v.getWeekendRate());

    List<String> images = new ArrayList<>();

    for (VenuePhoto p : v.getPhotos()) {
        images.add(baseUrl + "/api/venues/photo/" + p.getId());
    }

    result.put("photos", images);

    return result;
}

    // update venues
    public Venue updateVenue(Long id, Venue updatedVenue, Long userId) {

        Venue venue = getVenueById(id);

        if (!venue.getOwnerId().equals(userId)) {
            throw new RuntimeException("Not your venue");
        }

        // time validation
        if (updatedVenue.getOpenTime() != null && updatedVenue.getCloseTime() != null) {

            LocalTime open;
            LocalTime close;

            try {
                open = LocalTime.parse(updatedVenue.getOpenTime());
                close = LocalTime.parse(updatedVenue.getCloseTime());
            } catch (Exception e) {
                throw new RuntimeException("Invalid time format (HH:mm required)");
            }

            if (!open.isBefore(close)) {
                throw new RuntimeException("Open time must be before close time");
            }

            venue.setOpenTime(updatedVenue.getOpenTime());
            venue.setCloseTime(updatedVenue.getCloseTime());
        }

        venue.setName(updatedVenue.getName());
        venue.setNoOfCourts(updatedVenue.getNoOfCourts());
        venue.setWeekendRate(updatedVenue.getWeekendRate());
        venue.setWeekdayRate(updatedVenue.getWeekdayRate());
        venue.setPhoneNo(updatedVenue.getPhoneNo());
        venue.setEmail(updatedVenue.getEmail());
        venue.setDescription(updatedVenue.getDescription());
        venue.setAddress(updatedVenue.getAddress());

        return venueRepository.save(venue);
    }

    // datele venue code
    public void deleteVenue(Long id, Long userId) {
        Venue venue = getVenueById(id);

        // id check validation
        if (!venue.getOwnerId().equals(userId)) {
            throw new RuntimeException("Not your venue");
        }

        venueRepository.delete(venue);
    }

    // filter bt time and date for booker
    public List<Map<String, Object>> filterVenues(String date, String time) {

        // validate date format
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format (yyyy-MM-dd required)");
        }

        // validate past time
        if (parsedDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("Past date not allowed");
        }

        // validate time format
        LocalTime parsedTime;
        try {
            parsedTime = LocalTime.parse(time);
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format (HH:mm required)");
        }

        // prevent past time selection for today
        if (parsedDate.equals(LocalDate.now()) &&
                parsedTime.getHour() <= LocalTime.now().getHour()) {
            throw new RuntimeException("Cannot select past time for today");
        }

        List<Venue> venues = venueRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Venue v : venues) {

            int open = Integer.parseInt(v.getOpenTime().split(":")[0]);
            int close = Integer.parseInt(v.getCloseTime().split(":")[0]);

            if (parsedTime.getHour() < open || parsedTime.getHour() >= close) {
                continue;
            }

            List<Booking> bookings = bookingRepository.findByVenueIdAndBookingDate(v.getId(), date);

            boolean available = false;

            for (int court = 1; court <= v.getNoOfCourts(); court++) {

                boolean booked = false;

                for (Booking b : bookings) {

                    boolean isActiveCart = "IN_CART".equals(b.getStatus()) &&
                            b.getCreatedAt().isAfter(
                                    java.time.LocalDateTime.now().minusMinutes(10));

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
                item.put("startingPrice", Math.min(v.getWeekdayRate(), v.getWeekendRate()));

                List<VenuePhoto> photos = venuePhotoRepository.findByVenue_Id(v.getId());
                item.put("photo", photos.isEmpty() ? null : baseUrl + "/api/venues/photo/" + photos.get(0).getId());

                result.add(item);
            }
        }

        return result;
    }


   // filter by location (EXACT MATCH - CASE INSENSITIVE)
public List<Map<String, Object>> filterByLocation(String location) {

    List<Venue> venues;

    if (location == null || location.trim().isEmpty()) {
        venues = venueRepository.findAll();
    } else {
        venues = venueRepository.findByAddressContainingIgnoreCase(location.trim());
    }

    List<Map<String, Object>> result = new ArrayList<>();

    for (Venue v : venues) {

        Map<String, Object> item = new HashMap<>();
        item.put("id", v.getId());
        item.put("name", v.getName());
        item.put("location", v.getAddress());
        item.put("courts", v.getNoOfCourts());
        item.put("startingPrice", Math.min(v.getWeekdayRate(), v.getWeekendRate()));

        List<VenuePhoto> photos = venuePhotoRepository.findByVenue_Id(v.getId());

        item.put("photo",
                photos.isEmpty()
                        ? null
                        : baseUrl + "/api/venues/photo/" + photos.get(0).getId());

        result.add(item);
    }

    return result;
}

    // market place get all venues
    public List<Map<String, Object>> getMarketplaceVenues() {

        List<Venue> venues = venueRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Venue v : venues) {

            Map<String, Object> item = new HashMap<>();
            item.put("id", v.getId());
            item.put("name", v.getName());
            item.put("location", v.getAddress());
            item.put("courts", v.getNoOfCourts());
            item.put("startingPrice", Math.min(v.getWeekdayRate(), v.getWeekendRate()));

            List<VenuePhoto> photos = venuePhotoRepository.findByVenue_Id(v.getId());
            item.put("photo",
                    photos.isEmpty() ? null : baseUrl + "/api/venues/photo/" + photos.get(0).getId());

            result.add(item);
        }

        return result;
    }

    // uplaod image
    public String uploadPhoto(Long venueId, MultipartFile file, Long userId) throws Exception {

        Venue venue = venueRepository.findById(venueId).orElseThrow(() -> new RuntimeException("Venue not found"));

        if (!venue.getOwnerId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (venuePhotoRepository.findByVenue_Id(venueId).size() >= 5) {
            throw new RuntimeException("Maximum 5 photos allowed");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Only image files allowed");
        }

        VenuePhoto photo = new VenuePhoto();
        photo.setVenue(venue);
        photo.setFileName(file.getOriginalFilename());
        photo.setContentType(file.getContentType());
        photo.setData(file.getBytes());

        venuePhotoRepository.save(photo);

        return "Photo uploaded successfully";
    }

    // delete photos
    public String deletePhoto(Long venueId, Long photoId, Long userId) {

        Venue venue = venueRepository.findById(venueId).orElseThrow(() -> new RuntimeException("Venue not found"));

        if (!venue.getOwnerId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        VenuePhoto photo = venuePhotoRepository.findByIdAndVenue_Id(photoId, venueId)
                .orElseThrow(() -> new RuntimeException("Photo not found for this venue"));

        venuePhotoRepository.delete(photo);

        return "Photo deleted successfully";
    }
}