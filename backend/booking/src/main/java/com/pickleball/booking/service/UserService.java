package com.pickleball.booking.service;

import com.pickleball.booking.entity.User;
import com.pickleball.booking.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // GET PROFILE
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // UPDATE PROFILE
    public User updateProfile(Long userId, User updated) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔥 NAME VALIDATION
        if (updated.getName() == null || updated.getName().trim().length() < 3) {
            throw new RuntimeException("Name must be at least 3 characters");
        }

        // 🔥 EMAIL VALIDATION
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (updated.getEmail() == null ||
                !Pattern.matches(emailRegex, updated.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        // 🔥 EMAIL DUPLICATE CHECK
        if (!user.getEmail().equals(updated.getEmail()) &&
                userRepository.existsByEmail(updated.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        user.setName(updated.getName().trim());
        user.setEmail(updated.getEmail().trim());

        // 🔥 PASSWORD UPDATE (OPTIONAL)
        if (updated.getPassword() != null &&
                !updated.getPassword().trim().isEmpty()) {

            if (updated.getPassword().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }

            user.setPassword(passwordEncoder.encode(updated.getPassword()));
        }

        return userRepository.save(user);
    }
}