package com.pickleball.booking.controller;

import com.pickleball.booking.service.AvailabilityService;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

private Long getUserId() {

    Object principal = SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();

    if (principal instanceof Long) {
        return (Long) principal;
    }

    if (principal instanceof String) {
        if ("anonymousUser".equals(principal)) return null;

        try {
            return Long.parseLong((String) principal);
        } catch (Exception e) {
            return null;
        }
    }

    return null;
}

    @GetMapping("/{venueId}")
    public Map<String, Object> getAvailability(@PathVariable Long venueId, @RequestParam String date) {
        System.out.println(
    SecurityContextHolder.getContext().getAuthentication().getPrincipal().getClass()
);
        return availabilityService.checkAvailability(venueId, date, getUserId());
    }
}