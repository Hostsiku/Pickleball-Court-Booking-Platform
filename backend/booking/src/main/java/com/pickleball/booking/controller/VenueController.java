package com.pickleball.booking.controller;

import com.pickleball.booking.entity.Venue;
import com.pickleball.booking.service.VenueService;
import com.pickleball.booking.config.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final JwtUtil jwtUtil;

    // 🔐 Extract userId from token
    private Long getUserId(String token) {
        Claims claims = jwtUtil.extractClaims(token.substring(7));
        return Long.parseLong(claims.getSubject());
    }

    private String getUserRole(String token) {
        Claims claims = jwtUtil.extractClaims(token.substring(7));
        return (String) claims.get("role");
    }

    // ✅ CREATE
    @PostMapping
    public Venue createVenue(@RequestBody Venue venue,
                             @RequestHeader("Authorization") String token) {

        if (!getUserRole(token).equals("OWNER")) {
            throw new RuntimeException("Only owner can create venue");
        }

        return venueService.createVenue(venue, getUserId(token));
    }

    // ✅ GET ALL
    @GetMapping
    public List<Venue> getAllVenues() {
        return venueService.getAllVenues();
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public Venue getVenue(@PathVariable Long id) {
        return venueService.getVenueById(id);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public Venue updateVenue(@PathVariable Long id,
                             @RequestBody Venue venue,
                             @RequestHeader("Authorization") String token) {

        return venueService.updateVenue(id, venue, getUserId(token));
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public String deleteVenue(@PathVariable Long id,
                              @RequestHeader("Authorization") String token) {

        venueService.deleteVenue(id, getUserId(token));
        return "Deleted successfully";
    }
}