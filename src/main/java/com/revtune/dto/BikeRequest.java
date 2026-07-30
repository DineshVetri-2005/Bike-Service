package com.revtune.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BikeRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "Bike number is required")
    private String bikeNumber;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Bike type is required")
    private String bikeType;

    @NotNull(message = "Manufacturing year is required")
    private Integer manufacturingYear;
}
