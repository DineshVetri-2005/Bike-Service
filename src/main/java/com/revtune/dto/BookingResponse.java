package com.revtune.dto;

import com.revtune.model.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private Long userId;
    private Long bikeId;
    private Long serviceId;
    private LocalDate bookingDate;
    private LocalTime bookingTime;
    private BookingStatus status;
    private Double totalAmount;
}
