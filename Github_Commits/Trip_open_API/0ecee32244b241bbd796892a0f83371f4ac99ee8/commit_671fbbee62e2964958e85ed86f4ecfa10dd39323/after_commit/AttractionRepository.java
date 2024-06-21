package com.ssafy.i5i.hotelAPI.domain.hotel.repository;

import com.ssafy.i5i.hotelAPI.domain.hotel.entity.Attraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AttractionRepository extends JpaRepository<Attraction, Long> {

    Optional<Attraction> findTopByTitle(@Param("title") String title);
}
