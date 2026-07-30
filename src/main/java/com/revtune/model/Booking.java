package com.revtune.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "userId is required")
    @Column(nullable = false)
    private Long userId;

    @NotNull(message = "bikeId is required")
    @Column(nullable = false)
    private Long bikeId;

    @NotNull(message = "serviceId is required")
    @Column(nullable = false)
    private Long serviceId;

    @NotNull(message = "Booking date is required")
    @Column(nullable = false)
    private LocalDate bookingDate;

    @NotNull(message = "Booking time is required")
    @Column(nullable = false)
    private LocalTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.BOOKED;

    @NotNull(message = "Total amount is required")
    @Column(nullable = false)
    private Double totalAmount;
}
