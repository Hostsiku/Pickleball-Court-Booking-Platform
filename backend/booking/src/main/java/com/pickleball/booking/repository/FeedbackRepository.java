package com.pickleball.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pickleball.booking.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}