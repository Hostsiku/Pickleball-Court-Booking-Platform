package com.pickleball.booking.controller;

import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.entity.VenuePhoto;
import com.pickleball.booking.service.VenueService;
import com.pickleball.booking.service.BookingService;
import com.pickleball.booking.repository.VenuePhotoRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final BookingService bookingService;
    private final VenuePhotoRepository venuePhotoRepository;

    private Long getUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // create venue mapping
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping
    public Venue createVenue(@Valid @RequestBody Venue venue) {

        return venueService.createVenue(venue, getUserId());
    }

    // get all venues
    @GetMapping
    public List<Venue> getAllVenues() {
        return venueService.getAllVenues();
    }

    // get venues by id
    @GetMapping("/{id}")
    public Venue getVenue(@PathVariable Long id) {
        return venueService.getVenueById(id);
    }

    // update venue mapping
    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{id}")
    public Venue updateVenue(@PathVariable Long id, @RequestBody Venue venue) {

        return venueService.updateVenue(id, venue, getUserId());
    }

    // delete venue mapping
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    public String deleteVenue(@PathVariable Long id) {

        venueService.deleteVenue(id, getUserId());
        return "Deleted successfully";
    }

    // owner dashboard get bookings mapping
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{venueId}/bookings")
    public List<Map<String, Object>> getOwnerBookings(@PathVariable Long venueId,
            @RequestParam(required = false) String date) {

        return bookingService.getOwnerBookings(venueId, getUserId(), date);
    }

    // booker market place
    @GetMapping("/marketplace")
    public List<Map<String, Object>> getMarketplace() {
        return venueService.getMarketplaceVenues();
    }

    // filter for booking available
    @GetMapping("/marketplace/filter")
    public List<Map<String, Object>> filterVenues(@RequestParam String date, @RequestParam String time) {

        if (date == null || time == null) {
            throw new RuntimeException("Date and time are required");
        }

        return venueService.filterVenues(date, time);
    }

    // upload photes
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{venueId}/upload")
    public String uploadPhoto(@PathVariable Long venueId, @RequestParam("file") MultipartFile file) throws Exception {

        Long userId = getUserId();

        return venueService.uploadPhoto(venueId, file, userId);
    }

    // get images
    @GetMapping("/photo/{id}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {

        VenuePhoto photo = venuePhotoRepository.findById(id).orElseThrow(() -> new RuntimeException("Photo not found"));

        return ResponseEntity.ok().header("Content-Type", photo.getContentType()).body(photo.getData());
    }

    // delete venue
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{venueId}/photos/{photoId}")
    public String deletePhoto(@PathVariable Long venueId, @PathVariable Long photoId) {

        Long userId = getUserId();

        return venueService.deletePhoto(venueId, photoId, userId);
    }
}