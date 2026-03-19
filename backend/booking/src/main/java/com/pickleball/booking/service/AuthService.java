package com.pickleball.booking.service;

import com.pickleball.booking.config.JwtUtil;
import com.pickleball.booking.dto.AuthRequest;
import com.pickleball.booking.dto.RegisterRequest;
import com.pickleball.booking.entity.User;
import com.pickleball.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {

        // VALIDATE ALL FIELDS
        if (request.getName() == null || request.getName().isEmpty()
                || request.getEmail() == null || request.getEmail().isEmpty()
                || request.getPassword() == null || request.getPassword().isEmpty()
                || request.getRole() == null || request.getRole().isEmpty()) {

            throw new RuntimeException("All fields are required");
        }

        // VALIDATE ROLE
        if (!request.getRole().equalsIgnoreCase("OWNER")
                && !request.getRole().equalsIgnoreCase("BOOKER")) {
            throw new RuntimeException("Role must be OWNER or BOOKER");
        }

        // DUPLICATE EMAIL
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole().toUpperCase());

        userRepository.save(user);

        return "User registered successfully";
    }

    public String login(AuthRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return jwtUtil.generateToken(user.getId(), user.getRole());
    }
}