package com.revtune.controller;

import com.revtune.dto.BikeRequest;
import com.revtune.dto.BikeResponse;
import com.revtune.service.BikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bikes")
@RequiredArgsConstructor
public class BikeController {

    private final BikeService bikeService;

    @PostMapping
    public ResponseEntity<BikeResponse> addBike(@Valid @RequestBody BikeRequest request) {
        BikeResponse response = bikeService.addBike(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BikeResponse>> getBikesByUser(@PathVariable Long userId) {
        List<BikeResponse> response = bikeService.getBikesByUser(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BikeResponse> updateBike(@PathVariable Long id, @Valid @RequestBody BikeRequest request) {
        BikeResponse response = bikeService.updateBike(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBike(@PathVariable Long id) {
        bikeService.deleteBike(id);
        return ResponseEntity.noContent().build();
    }
}
