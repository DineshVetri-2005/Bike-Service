package com.revtune.config;

import com.revtune.model.Service;
import com.revtune.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ServiceRepository serviceRepository;

    @Override
    public void run(String... args) {
        if (serviceRepository.count() == 0) {
            serviceRepository.save(new Service(null, "Engine Oil Change", "Replace engine oil and filter", 350.0, "30 mins"));
            serviceRepository.save(new Service(null, "General Service", "Full bike checkup and tune-up", 600.0, "2 hours"));
            serviceRepository.save(new Service(null, "Brake Inspection", "Check and adjust brake pads and cables", 150.0, "20 mins"));
            serviceRepository.save(new Service(null, "Chain Maintenance", "Chain cleaning, lubrication and adjustment", 100.0, "15 mins"));
            serviceRepository.save(new Service(null, "Battery Check", "Battery health check and terminal cleaning", 120.0, "20 mins"));
        }
    }
}
