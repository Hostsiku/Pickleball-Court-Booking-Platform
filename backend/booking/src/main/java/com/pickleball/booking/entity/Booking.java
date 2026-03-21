package com.pickleball.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bookings", uniqueConstraints = @UniqueConstraint(columnNames = {
           "venueId", "courtId", "bookingDate", "startTime"
       }))
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long venueId;
    private int courtId;

    private String bookingDate;
    private String startTime;
    private String endTime;

    private Long userId;

    private String status; 

    private LocalDateTime createdAt;
}