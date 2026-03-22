package com.pickleball.booking.repository;

import com.pickleball.booking.entity.VenuePhoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface VenuePhotoRepository extends JpaRepository<VenuePhoto, Long> {
    List<VenuePhoto> findByVenue_Id(Long venueId);

    Optional<VenuePhoto> findByIdAndVenue_Id(Long id, Long venueId);
}