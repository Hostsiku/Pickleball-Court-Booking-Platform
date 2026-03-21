package com.pickleball.booking.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venue_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VenuePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long venueId;

    private String fileName;
    private String contentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;
}