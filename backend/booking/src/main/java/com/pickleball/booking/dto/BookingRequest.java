package com.pickleball.booking.dto;
import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class BookingRequest {

    @NotNull(message = "Venue ID required")
    private Long venueId;

    @Min(value = 1, message = "Invalid court ID")
    private int courtId;

    @NotBlank(message = "Date required")
    private String date;

    @NotBlank(message = "Start time required")
    private String startTime;

    @NotBlank(message = "End time required")
    private String endTime;
}