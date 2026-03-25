package com.pickleball.booking.controller;

import com.pickleball.booking.dto.AuthRequest;
import com.pickleball.booking.dto.RegisterRequest;
import com.pickleball.booking.service.AuthService;
import lombok.RequiredArgsConstructor;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody AuthRequest request) {
    return authService.login(request);
}
}