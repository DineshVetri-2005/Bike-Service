package com.revtune.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BikeResponse {
    private Long id;
    private Long userId;
    private String bikeNumber;
    private String brand;
    private String model;
    private String bikeType;
    private Integer manufacturingYear;
}
