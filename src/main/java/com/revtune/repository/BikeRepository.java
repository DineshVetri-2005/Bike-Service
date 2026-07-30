package com.revtune.repository;

import com.revtune.model.Bike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BikeRepository extends JpaRepository<Bike, Long> {
    List<Bike> findByUserId(Long userId);
    boolean existsByBikeNumber(String bikeNumber);
}
