package com.pickleball.booking.controller;

import com.pickleball.booking.service.AvailabilityService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    private Long getUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @PreAuthorize("hasRole('BOOKER')")
    @GetMapping("/{venueId}")
    public Map<String, Object> getAvailability(@PathVariable Long venueId, @RequestParam String date) {
        return availabilityService.checkAvailability(venueId, date, getUserId());
    }
}