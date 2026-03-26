package com.pickleball.booking.service;

import com.pickleball.booking.entity.Feedback;
import com.pickleball.booking.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import java.time.*;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public String submitFeedback(Long userId, String message) {

        if (message == null || message.trim().isEmpty()) {
            throw new RuntimeException("Feedback cannot be empty");
        }

        Feedback f = new Feedback();
        f.setUserId(userId);
        f.setMessage(message.trim());
        f.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(f);

        return "Feedback submitted successfully";
    }
}
