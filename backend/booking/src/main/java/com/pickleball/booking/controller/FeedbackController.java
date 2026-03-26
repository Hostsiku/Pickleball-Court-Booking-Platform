package com.pickleball.booking.controller;

import com.pickleball.booking.service.FeedbackService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    private Long getUserId() {
        return (Long) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @PreAuthorize("hasRole('BOOKER')")
    @PostMapping
    public String submitFeedback(@RequestBody Map<String, String> body) {
        return feedbackService.submitFeedback(getUserId(), body.get("message"));
    }
}