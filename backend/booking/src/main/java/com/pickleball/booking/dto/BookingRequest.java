package com.pickleball.booking.dto;

import lombok.Data;

@Data
public class BookingRequest {

    private Long venueId;
    private int courtId;
    private String date;
    private String startTime;
    private String endTime;
}