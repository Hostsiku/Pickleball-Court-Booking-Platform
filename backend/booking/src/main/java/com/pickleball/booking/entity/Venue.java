package com.pickleball.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long ownerId;

    private String name;

    private int noOfCourts;

    private String openTime;
    private String closeTime;

    private int weekendRate;
    private int weekdayRate;

    private String phoneNo;
    private String email;

    private String description;

    private String address;

@OneToMany(mappedBy = "venueId", cascade = CascadeType.ALL)
private List<VenuePhoto> photos;
}