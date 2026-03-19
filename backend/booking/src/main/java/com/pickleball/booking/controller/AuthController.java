package com.pickleball.booking.controller;

import com.pickleball.booking.dto.AuthRequest;
import com.pickleball.booking.dto.RegisterRequest;
import com.pickleball.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {
    return authService.login(request);
}
}