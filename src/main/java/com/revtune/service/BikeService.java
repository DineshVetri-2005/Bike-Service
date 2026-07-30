package com.revtune.service;

import com.revtune.dto.BikeRequest;
import com.revtune.dto.BikeResponse;
import com.revtune.exception.DuplicateResourceException;
import com.revtune.exception.ResourceNotFoundException;
import com.revtune.model.Bike;
import com.revtune.repository.BikeRepository;
import com.revtune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BikeService {

    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;

    public BikeResponse addBike(BikeRequest request) {
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        if (bikeRepository.existsByBikeNumber(request.getBikeNumber())) {
            throw new DuplicateResourceException("A bike with this number is already registered");
        }

        Bike bike = new Bike();
        bike.setUserId(request.getUserId());
        bike.setBikeNumber(request.getBikeNumber());
        bike.setBrand(request.getBrand());
        bike.setModel(request.getModel());
        bike.setBikeType(request.getBikeType());
        bike.setManufacturingYear(request.getManufacturingYear());

        Bike saved = bikeRepository.save(bike);
        return toResponse(saved);
    }

    public List<BikeResponse> getBikesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return bikeRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BikeResponse updateBike(Long id, BikeRequest request) {
        Bike bike = bikeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bike not found with id: " + id));

        if (!bike.getBikeNumber().equals(request.getBikeNumber())
                && bikeRepository.existsByBikeNumber(request.getBikeNumber())) {
            throw new DuplicateResourceException("A bike with this number is already registered");
        }

        bike.setBikeNumber(request.getBikeNumber());
        bike.setBrand(request.getBrand());
        bike.setModel(request.getModel());
        bike.setBikeType(request.getBikeType());
        bike.setManufacturingYear(request.getManufacturingYear());

        Bike updated = bikeRepository.save(bike);
        return toResponse(updated);
    }

    public void deleteBike(Long id) {
        Bike bike = bikeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bike not found with id: " + id));
        bikeRepository.delete(bike);
    }

    private BikeResponse toResponse(Bike bike) {
        return new BikeResponse(
                bike.getId(),
                bike.getUserId(),
                bike.getBikeNumber(),
                bike.getBrand(),
                bike.getModel(),
                bike.getBikeType(),
                bike.getManufacturingYear()
        );
    }
}
