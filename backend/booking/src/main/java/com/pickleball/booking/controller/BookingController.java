package com.pickleball.booking.controller;

import com.pickleball.booking.dto.BookingRequest;
import com.pickleball.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    private Long getUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // add to cart
    @PreAuthorize("hasRole('BOOKER')")
    @PostMapping("/add")
    public String addToCart(@Valid @RequestBody BookingRequest request) {
        return bookingService.addToCart(request, getUserId());
    }

    // get cart items
    @PreAuthorize("hasRole('BOOKER')")
    @GetMapping("/cart")
    public Map<String, Object> getCart() {
        return bookingService.getCart(getUserId());
    }

    // remove cart items
    @PreAuthorize("hasRole('BOOKER')")
    @DeleteMapping("/cart/{id}")
    public String remove(@PathVariable Long id) {
        return bookingService.removeFromCart(id, getUserId());
    }

    // checkout mappling
    @PreAuthorize("hasRole('BOOKER')")
    @PostMapping("/checkout")
    public Map<String, Object> checkout() {
        return bookingService.checkout(getUserId());
    }

    // get history bookings
    @PreAuthorize("hasRole('BOOKER')")
    @GetMapping("/history")
    public List<Map<String, Object>> history() {
        return bookingService.history(getUserId());
    }

    // reschedule code
    @PreAuthorize("hasRole('BOOKER')")
    @PutMapping("/reschedule/{id}")
    public String reschedule(@PathVariable Long id, @Valid @RequestBody BookingRequest request) {
        return bookingService.reschedule(id, request, getUserId());
    }

    // cancel booking
    @PreAuthorize("hasRole('BOOKER')")
    @DeleteMapping("/cancel/{id}")
    public String cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id, getUserId());
    }
}