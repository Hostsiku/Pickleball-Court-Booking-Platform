package com.pickleball.booking.repository;

import com.pickleball.booking.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface VenueRepository extends JpaRepository<Venue, Long> {

List<Venue> findByAddressContainingIgnoreCase(String address);
}