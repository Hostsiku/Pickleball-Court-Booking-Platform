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

    // add to cart 
    @PostMapping("/add")
    public String addToCart(@RequestBody BookingRequest request) {
        return bookingService.addToCart(request, getUserId());
    }

    // get cart items
    @GetMapping("/cart")
    public Map<String, Object> getCart() {
        return bookingService.getCart(getUserId());
    }

    // remove cart items
@DeleteMapping("/{id}")
public String remove(@PathVariable Long id) {
    return bookingService.removeFromCart(id, getUserId());
}

    // checkout mappling
    @PostMapping("/checkout")
    public Map<String, Object> checkout() {
        return bookingService.checkout(getUserId());
    }

    // get history bookings
@GetMapping("/history")
public List<Map<String, Object>> history() {
    return bookingService.history(getUserId());
}

// reschedule code
@PutMapping("/reschedule/{id}")
public String reschedule(@PathVariable Long id, @RequestBody BookingRequest request) {
    return bookingService.reschedule(id, request, getUserId());
}

// cancel booking
@DeleteMapping("/cancel/{id}")
public String cancelBooking(@PathVariable Long id) {
    return bookingService.cancelBooking(id, getUserId());
}
}