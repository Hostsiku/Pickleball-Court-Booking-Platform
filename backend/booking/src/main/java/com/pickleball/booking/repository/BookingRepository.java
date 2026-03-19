package com.pickleball.booking.repository;

import com.pickleball.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByVenueIdAndBookingDate(Long venueId, String bookingDate);

    List<Booking> findByVenueIdAndCourtIdAndBookingDate(Long venueId, int courtId, String bookingDate);

    Optional<Booking> findByVenueIdAndCourtIdAndBookingDateAndStartTime(
            Long venueId,
            int courtId,
            String bookingDate,
            String startTime
    );

    List<Booking> findByUserIdAndStatus(Long userId, String status);

    List<Booking> findByUserIdAndStatusOrderByBookingDate(Long userId, String status);

    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    List<Booking> findByStatusAndCreatedAtBefore(String status, LocalDateTime time);
}