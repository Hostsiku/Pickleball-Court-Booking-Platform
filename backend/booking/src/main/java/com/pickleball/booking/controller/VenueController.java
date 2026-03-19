package com.pickleball.booking.controller;

import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.service.VenueService;
import com.pickleball.booking.config.JwtUtil;
import com.pickleball.booking.service.BookingService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final JwtUtil jwtUtil;
    private final BookingService bookingService;

    private Long getUserId(String token) {
        Claims claims = jwtUtil.extractClaims(token.substring(7));
        return Long.parseLong(claims.getSubject());
    }

    private String getUserRole(String token) {
        Claims claims = jwtUtil.extractClaims(token.substring(7));
        return (String) claims.get("role");
    }

    @PostMapping
    public Venue createVenue(@RequestBody Venue venue,
                            @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can create venue");
        }

        return venueService.createVenue(venue, getUserId(token));
    }

    @GetMapping
    public List<Venue> getAllVenues() {
        return venueService.getAllVenues();
    }

    @GetMapping("/{id}")
    public Venue getVenue(@PathVariable Long id) {
        return venueService.getVenueById(id);
    }

    @PutMapping("/{id}")
    public Venue updateVenue(@PathVariable Long id,
                            @RequestBody Venue venue,
                            @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can update venue");
        }

        return venueService.updateVenue(id, venue, getUserId(token));
    }

    @DeleteMapping("/{id}")
    public String deleteVenue(@PathVariable Long id,
                             @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can delete venue");
        }

        venueService.deleteVenue(id, getUserId(token));
        return "Deleted successfully";
    }

    // OWNER DASHBOARD
    @GetMapping("/{venueId}/bookings")
    public List<Map<String, Object>> getOwnerBookings(
            @PathVariable Long venueId,
            @RequestParam(required = false) String date,
            @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can view bookings");
        }

        return bookingService.getOwnerBookings(
                venueId,
                getUserId(token),
                date
        );
    }

    // MARKETPLACE
    @GetMapping("/marketplace")
    public List<Map<String, Object>> getMarketplace() {
        return venueService.getMarketplaceVenues();
    }

    // FILTER
    @GetMapping("/marketplace/filter")
    public List<Map<String, Object>> filterVenues(
            @RequestParam String date,
            @RequestParam String time) {

        if (date == null || time == null) {
            throw new RuntimeException("Date and time are required");
        }

        return venueService.filterVenues(date, time);
    }
}