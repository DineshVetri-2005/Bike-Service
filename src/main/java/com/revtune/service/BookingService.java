package com.revtune.service;

import com.revtune.dto.BookingRequest;
import com.revtune.dto.BookingResponse;
import com.revtune.exception.ResourceNotFoundException;
import com.revtune.model.Booking;
import com.revtune.model.BookingStatus;
import com.revtune.repository.BikeRepository;
import com.revtune.repository.BookingRepository;
import com.revtune.repository.ServiceRepository;
import com.revtune.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final BikeRepository bikeRepository;
    private final ServiceRepository serviceRepository;

    public BookingResponse createBooking(BookingRequest request) {
        if (!userRepository.existsById(request.getUserId())) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        if (!bikeRepository.existsById(request.getBikeId())) {
            throw new ResourceNotFoundException("Bike not found with id: " + request.getBikeId());
        }
        com.revtune.model.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + request.getServiceId()));

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setBikeId(request.getBikeId());
        booking.setServiceId(request.getServiceId());
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setStatus(BookingStatus.BOOKED);
        booking.setTotalAmount(service.getPrice());

        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    public List<BookingResponse> getBookingsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return bookingRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return toResponse(booking);
    }

    public BookingResponse updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        booking.setStatus(status);
        Booking updated = bookingRepository.save(booking);
        return toResponse(updated);
    }

    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        bookingRepository.delete(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUserId(),
                booking.getBikeId(),
                booking.getServiceId(),
                booking.getBookingDate(),
                booking.getBookingTime(),
                booking.getStatus(),
                booking.getTotalAmount()
        );
    }
}
