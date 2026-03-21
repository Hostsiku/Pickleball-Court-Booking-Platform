package com.pickleball.booking.controller;

import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.entity.VenuePhoto;
import com.pickleball.booking.service.VenueService;
import com.pickleball.booking.config.JwtUtil;
import com.pickleball.booking.service.BookingService;
import com.pickleball.booking.repository.VenuePhotoRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final JwtUtil jwtUtil;
    private final BookingService bookingService;
    private final VenuePhotoRepository venuePhotoRepository;

    private Long getUserId(String token) {
        Claims claims = jwtUtil.extractClaims(token.substring(7));
        return Long.parseLong(claims.getSubject());
    }

    private String getUserRole(String token) {
        Claims claims = jwtUtil.extractClaims(token.substring(7));
        return (String) claims.get("role");
    }

    // create venue mapping
    @PostMapping
    public Venue createVenue(@RequestBody Venue venue, @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can create venue");
        }

        return venueService.createVenue(venue, getUserId(token));
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
    @PutMapping("/{id}")
    public Venue updateVenue(@PathVariable Long id, @RequestBody Venue venue, @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can update venue");
        }

        return venueService.updateVenue(id, venue, getUserId(token));
    }

    // delete venue mapping
    @DeleteMapping("/{id}")
    public String deleteVenue(@PathVariable Long id,@RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can delete venue");
        }

        venueService.deleteVenue(id, getUserId(token));
        return "Deleted successfully";
    }

    // owner dashboard get bookings mappling
    @GetMapping("/{venueId}/bookings")
    public List<Map<String, Object>> getOwnerBookings(@PathVariable Long venueId, @RequestParam(required = false) String date,
            @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can view bookings");
        }

        return bookingService.getOwnerBookings(venueId, getUserId(token), date);
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
    @PostMapping("/{venueId}/upload")
public String uploadPhoto(@PathVariable Long venueId, @RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token
) throws Exception {

    Long userId = getUserId(token);

    return venueService.uploadPhoto(venueId, file, userId);
}

// get images
@GetMapping("/photo/{id}")
public ResponseEntity<byte[]> getPhoto(@PathVariable Long id) {

    VenuePhoto photo = venuePhotoRepository.findById(id).orElseThrow(() -> new RuntimeException("Photo not found"));

    return ResponseEntity.ok().header("Content-Type", photo.getContentType()).body(photo.getData());
}

// delete venue
@DeleteMapping("/{venueId}/photos/{photoId}")
public String deletePhoto(@PathVariable Long venueId, @PathVariable Long photoId, @RequestHeader("Authorization") String token) {

    Long userId = getUserId(token); 

    if (!getUserRole(token).equals("OWNER")) {
        throw new RuntimeException("Only owner can delete photos");
    }

    return venueService.deletePhoto(venueId, photoId, userId);
}
}