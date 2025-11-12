# Nexus Platform - Spring Modulith Backend

## Overview

This is the consolidated Spring Modulith application that combines all microservices into a single deployable unit while maintaining module boundaries.

## Architecture

### Module Structure

- **`com.nexus.shared`** - Shared services used across all domains
  - `iam` - Identity and Access Management
  - `userprofile` - User Profile Management
  - `address` - Address Management
  - `payment` - Payment Processing
  - `gateway` - API Gateway (if needed)

- **`com.nexus.ecommerce`** - Ecommerce domain modules
  - `cart` - Shopping Cart
  - `catalog` - Product Catalog
  - `checkout` - Checkout Process
  - `order` - Order Management
  - `inventory` - Inventory Tracking
  - `promo` - Promotions and Discounts
  - `search` - Search Functionality
  - `fulfilment` - Order Fulfillment

- **`com.nexus.healthcare`** - Healthcare domain modules
  - `doctor` - Doctor Management
  - `patient` - Patient Records
  - `appointment` - Appointment Scheduling
  - `session` - Session Management
  - `consultation` - Consultations
  - `notification` - Notifications
  - `report` - Reports

## Key Changes from Microservices

1. **Single Database**: All modules use the same PostgreSQL database (`nexus_db`). Data isolation is maintained via `domain_id` + `tenant_id` columns.

2. **Direct Service Calls**: Inter-module communication uses direct service method calls instead of HTTP. See `TenantService` for an example.

3. **Java 21 Compatibility**: Replaced Java 25 `ScopedValue` with Spring Security's `SecurityContextHolder` for tenant/user context.

4. **Consolidated Configuration**: Single `application.yml` with all service configurations.

5. **Unified Migrations**: All Flyway migrations are in `src/main/resources/db/migration/` with unique version numbers.

## Running the Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Or run the JAR
java -jar target/nexus-platform-1.0.0-SNAPSHOT.jar
```

## Docker Build

```bash
# Build Docker image using Jib
mvn jib:build

# Or build locally
mvn jib:dockerBuild
```

## Configuration

All configuration is in `src/main/resources/application.yml`. Key settings:

- Database: `jdbc:postgresql://localhost:5432/nexus_db`
- Server Port: `8080`
- Redis: `localhost:6379`
- Kafka: `localhost:9092`

## Module Boundaries

Spring Modulith enforces module boundaries at compile time. Modules can only access:
- Public APIs of other modules (interfaces, DTOs)
- Shared modules (com.nexus.shared.*)

## Future Optimizations

1. **Replace HTTP Client Calls**: Some ecommerce services still use HTTP clients to call each other. These should be replaced with direct service method calls for better performance.

2. **Event-Driven Communication**: Consider using Spring Modulith Events instead of Kafka for inter-module communication.

3. **Service Interfaces**: Create service interfaces for cross-module communication to maintain loose coupling.

## Migration Notes

- All package names changed from `com.ecom.*` to `com.nexus.*`
- All Application classes removed (using single `NexusApplication`)
- HTTP service URLs updated to `localhost:8080`
- Database migrations consolidated and renumbered

