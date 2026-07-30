package com.revtune.service;

import com.revtune.dto.ServiceRequest;
import com.revtune.dto.ServiceResponse;
import com.revtune.exception.ResourceNotFoundException;
import com.revtune.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business logic for the bike-service catalog (Service entity).
 * Named ServiceCatalogService — not @Service annotated as "ServiceService" to
 * avoid any ambiguity with org.springframework.stereotype.Service.
 */
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceCatalogService {

    private final ServiceRepository serviceRepository;

    public ServiceResponse createService(ServiceRequest request) {
        com.revtune.model.Service service = new com.revtune.model.Service();
        service.setServiceName(request.getServiceName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setEstimatedTime(request.getEstimatedTime());

        com.revtune.model.Service saved = serviceRepository.save(service);
        return toResponse(saved);
    }

    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ServiceResponse getServiceById(Long id) {
        com.revtune.model.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with id: " + id));
        return toResponse(service);
    }

    private ServiceResponse toResponse(com.revtune.model.Service service) {
        return new ServiceResponse(
                service.getId(),
                service.getServiceName(),
                service.getDescription(),
                service.getPrice(),
                service.getEstimatedTime()
        );
    }
}
