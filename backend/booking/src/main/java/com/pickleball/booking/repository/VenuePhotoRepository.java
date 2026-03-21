package com.pickleball.booking.repository;

import com.pickleball.booking.entity.VenuePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface VenuePhotoRepository extends JpaRepository<VenuePhoto, Long> {
    List<VenuePhoto> findByVenueId(Long venueId);

    Optional<VenuePhoto> findByIdAndVenueId(Long id, Long venueId);
}