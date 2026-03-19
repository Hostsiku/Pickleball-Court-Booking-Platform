package com.pickleball.booking.controller;

import com.pickleball.booking.dto.BookingRequest;
import com.pickleball.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
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

    // Add to cart
    @PostMapping("/add")
    public String addToCart(@RequestBody BookingRequest request) {
        return bookingService.addToCart(request, getUserId());
    }

    //UPDATED CART API
    @GetMapping("/cart")
    public Map<String, Object> getCart() {
        return bookingService.getCart(getUserId());
    }

    // Remove
@DeleteMapping("/{id}")
public String remove(@PathVariable Long id) {
    return bookingService.removeFromCart(id, getUserId());
}

    // UPDATED CHECKOUT
    @PostMapping("/checkout")
    public Map<String, Object> checkout() {
        return bookingService.checkout(getUserId());
    }

    // HISTORY
@GetMapping("/history")
public List<Map<String, Object>> history() {
    return bookingService.history(getUserId());
}

// RESCHEDULE
@PutMapping("/reschedule/{id}")
public String reschedule(@PathVariable Long id,
                         @RequestBody BookingRequest request) {
    return bookingService.reschedule(id, request, getUserId());
}

// cancel booking
@DeleteMapping("/cancel/{id}")
public String cancelBooking(@PathVariable Long id) {
    return bookingService.cancelBooking(id, getUserId());
}
}