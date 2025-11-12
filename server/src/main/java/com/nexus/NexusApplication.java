package com.nexus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

/**
 * Nexus Platform - Main Application
 * 
 * <p>Spring Modulith application that consolidates all microservices
 * into a single deployable unit while maintaining module boundaries.
 * 
 * <p>All domains (ecommerce, healthcare, etc.) and shared services
 * (IAM, Payment, Address, etc.) are organized as modules within
 * this single application.
 */
@Modulith
@SpringBootApplication
public class NexusApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexusApplication.class, args);
    }
}

