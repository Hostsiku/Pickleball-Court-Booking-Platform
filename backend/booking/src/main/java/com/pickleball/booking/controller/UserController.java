package com.pickleball.booking.controller;

import com.pickleball.booking.entity.User;
import com.pickleball.booking.service.UserService;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

        private Long getUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @GetMapping("/profile")
    public User getProfile() {
        return userService.getProfile(getUserId());
    }

    @PutMapping("/profile")
    public User updateProfile(@RequestBody User user) {
        return userService.updateProfile(getUserId(), user);
    }
}
