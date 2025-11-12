<!-- 679780d0-c9e0-47a2-86cb-b0522c140add a2d71a10-fa15-4fad-b556-26169af398d9 -->
# Healthcare Domain Architecture Plan for Nexus Ecosystem

## Executive Summary

This plan outlines an optimized architecture for building a healthcare management platform (similar to Tiny Steps) within the Nexus multi-domain ecosystem. The strategy focuses on leveraging Nexus shared services, identifying reusable components, and creating a clean domain-specific service structure.

## Current State Analysis

### Tiny Steps Services (13+ microservices)

- **Core Domain Services**: doctor, patient, schedule, session, timing, consultation
- **Supporting Services**: payment, notification, report, address, auth, user, domain-config
- **Infrastructure**: api-gateway, service-registry

### Nexus Ecosystem Capabilities

- **Shared Services**: IAM, User Profile, Address, Payment, Gateway
- **Shared Libraries**: jwt-validation-starter, http-client-starter, tenant-context-starter, standard-response-starter, custom-error-starter
- **Patterns**: Domain ID + Tenant ID isolation, JWT-based auth, event-driven communication

## Architecture Strategy

### Phase 1: Service Classification & Mapping

#### 1.1 Map to Nexus Shared Services

- **Auth Service** → Use `shared/iam` (already handles JWT, registration, login)
- **User Service** → Use `shared/user-profile` (extend if needed for healthcare-specific profiles)
- **Address Service** → Use `shared/address` (already domain-aware with domain_id)
- **Payment Service** → Use `shared/payment` (extend for healthcare payment types)
- **API Gateway** → Use `shared/gateway` (configure routes for healthcare domain)

#### 1.2 Identify Domain-Specific Services

Create new services under `domains/healthcare/`:

- `doctor-service` - Doctor profiles, specializations, qualifications, awards
- `patient-service` - Patient records, medical history, allergies, medications, insurance
- `appointment-service` - Appointment booking, scheduling, status management
- `session-service` - Therapy sessions, session types, offerings
- `availability-service` - Doctor availability, time slots, time-off management
- `consultation-service` - Consultation records, notes, prescriptions
- `notification-service` - Healthcare-specific notifications (appointment reminders, test results)
- `report-service` - Medical reports, analytics, exports (includes appointment Excel exports - CRITICAL requirement)

#### 1.3 Extract to Shared Services (New)

Consider creating new shared services if reusable across domains:

- `shared/notification` - Generic notification service (if not domain-specific)
- `shared/search` - Generic search service with domain adapters

### Phase 2: Service Decomposition & Optimization

#### 2.1 Doctor Service Decomposition

**Current**: Single doctor-service with 8+ controllers

**Optimized Structure**:

```
domains/healthcare/
├── doctor-service/
│   ├── DoctorController (CRUD, search, verification)
│   ├── SpecializationController (master specializations)
│   ├── QualificationController
│   └── AwardController
```

**Key Optimizations**:

- Separate specialization master from doctor-specialization relationships
- Use Nexus domain_id for multi-domain specialization sharing
- Leverage shared/user-profile for basic user data, extend for doctor-specific fields

#### 2.2 Patient Service Decomposition

**Current**: Single patient-service with 9+ controllers

**Optimized Structure**:

```
domains/healthcare/
├── patient-service/
│   ├── PatientController (CRUD, search)
│   ├── PatientMedicalHistoryController
│   ├── PatientAllergyController
│   ├── PatientMedicationController
│   ├── PatientInsuranceController
│   └── PatientEmergencyContactController
```

**Key Optimizations**:

- Use shared/address for patient addresses
- Use shared/user-profile for basic patient profile
- Keep medical records domain-specific (HIPAA compliance)

#### 2.3 Scheduling & Availability Optimization

**Current**: Separate schedule-service and timing-service

**Optimized Structure**:

```
domains/healthcare/
├── appointment-service/ (combines schedule + timing logic)
│   ├── AppointmentController
│   ├── AvailabilityController
│   ├── TimeOffController
│   ├── SlotAvailabilityService (CRITICAL - slot conflict detection)
│   └── AppointmentConflictService (CRITICAL - booking validation)
```

**Rationale**: Appointment booking and availability are tightly coupled. Combining reduces inter-service calls and complexity.

**CRITICAL: Robust Slot Management & Conflict Detection**

The appointment service is the heart of the application and must handle complex slot management scenarios:

**1. Slot Availability Calculation Logic**:

- **Base Availability**: Doctor's regular availability schedule (day of week, time ranges)
- **Time Off Deduction**: Remove slots during doctor's time-off periods
- **Booked Slot Deduction**: Remove slots that are already booked
- **Cancelled Slot Restoration**: Re-add slots when appointments are cancelled
- **Session Duration Integration**: Consider session duration when calculating next available slot

**2. Session Duration Conflict Prevention**:

```
Example Scenario:
- Doctor availability: 9:00 AM - 12:00 PM (30-min slots)
- Session type: 45 minutes
- Booked appointment: 10:00 AM - 10:45 AM (45-min session)

Valid Next Slots:
✅ 11:00 AM (45-min session) - starts after 10:45 AM
✅ 10:45 AM (30-min session) - ends at 11:15 AM, no conflict
❌ 10:30 AM (45-min session) - would end at 11:15 AM, conflicts with 10:00 AM booking
❌ 9:30 AM (45-min session) - would end at 10:15 AM, conflicts with 10:00 AM booking
```

**3. Slot Calculation Algorithm**:

```java
// Pseudo-code for slot availability calculation
public List<AvailableSlot> calculateAvailableSlots(
    UUID doctorId, 
    LocalDate date, 
    UUID sessionTypeId
) {
    // 1. Get doctor's base availability for day of week
    List<Availability> baseAvailability = getDoctorAvailability(doctorId, date.getDayOfWeek());
    
    // 2. Get all time-offs for this date
    List<TimeOff> timeOffs = getTimeOffs(doctorId, date);
    
    // 3. Get all existing appointments for this date
    List<Appointment> existingAppointments = getAppointments(doctorId, date);
    
    // 4. Get session duration
    SessionType sessionType = getSessionType(sessionTypeId);
    Duration sessionDuration = sessionType.getDuration();
    
    // 5. Generate potential slots from base availability
    List<TimeSlot> potentialSlots = generateSlots(baseAvailability, sessionDuration);
    
    // 6. Filter out slots during time-offs
    potentialSlots = filterTimeOffs(potentialSlots, timeOffs);
    
    // 7. Filter out conflicting slots with existing appointments
    potentialSlots = filterConflictingSlots(potentialSlots, existingAppointments, sessionDuration);
    
    // 8. Return available slots
    return potentialSlots;
}

private List<TimeSlot> filterConflictingSlots(
    List<TimeSlot> slots, 
    List<Appointment> appointments, 
    Duration sessionDuration
) {
    return slots.stream()
        .filter(slot -> {
            LocalDateTime slotStart = slot.getStartTime();
            LocalDateTime slotEnd = slotStart.plus(sessionDuration);
            
            // Check if slot conflicts with any existing appointment
            return appointments.stream().noneMatch(apt -> {
                LocalDateTime aptStart = apt.getStartTime();
                LocalDateTime aptEnd = apt.getEndTime();
                
                // Conflict if slots overlap
                return slotStart.isBefore(aptEnd) && slotEnd.isAfter(aptStart);
            });
        })
        .collect(Collectors.toList());
}
```

**4. Appointment Booking Validation**:

- **Pre-booking Check**: Validate slot is still available (prevent race conditions)
- **Conflict Detection**: Check for overlapping appointments
- **Time Off Validation**: Ensure not booking during doctor's time off
- **Session Duration Validation**: Ensure session fits in available time window
- **Atomic Booking**: Use database transactions to prevent double-booking

**5. Cancellation & Slot Restoration**:

- **Cancellation**: Mark appointment as cancelled, restore slot immediately
- **Soft Delete**: Keep cancelled appointments for audit trail
- **Slot Recalculation**: Trigger slot recalculation on cancellation
- **Event Publishing**: Publish cancellation event for other services

**6. Real-time Slot Updates**:

- **Event-Driven**: Publish slot availability changes via Kafka
- **Cache Invalidation**: Invalidate slot cache when appointments are booked/cancelled
- **WebSocket Updates**: Push real-time slot updates to frontend
- **Optimistic Locking**: Use version numbers to prevent concurrent modifications

**7. Integration Points**:

**Doctor Service Integration**:

- Fetch doctor availability schedule
- Validate doctor exists and is active
- Get doctor's specializations for filtering

**Session Service Integration**:

- Fetch session types and durations
- Validate session type exists
- Get session pricing information
- Calculate appointment duration from session type

**Availability Service Integration** (within appointment-service):

- Manage doctor's regular availability
- Handle time-off requests
- Calculate available time windows
- Generate time slots based on availability

**8. Database Schema Considerations**:

```sql
-- Appointments table
CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    session_type_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL, -- Calculated from start_time + session_duration
    status VARCHAR(50) NOT NULL, -- SCHEDULED, CONFIRMED, CANCELLED, COMPLETED
    tenant_id UUID NOT NULL,
    domain_id VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL, -- For optimistic locking
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    
    -- Constraints
    CONSTRAINT chk_end_after_start CHECK (end_time > start_time),
    CONSTRAINT unique_doctor_slot UNIQUE (doctor_id, start_time, deleted) 
        WHERE deleted = FALSE AND status NOT IN ('CANCELLED')
);

-- Indexes for performance
CREATE INDEX idx_appointments_doctor_date ON appointments(doctor_id, start_time) 
    WHERE deleted = FALSE;
CREATE INDEX idx_appointments_patient ON appointments(patient_id) 
    WHERE deleted = FALSE;
CREATE INDEX idx_appointments_status ON appointments(status) 
    WHERE deleted = FALSE;
```

**9. Service Methods**:

```java
@Service
public class SlotAvailabilityService {
    
    /**
     * Get available slots for a doctor on a specific date
     * Considers: base availability, time-offs, existing appointments, session duration
     */
    List<AvailableSlot> getAvailableSlots(
        UUID doctorId, 
        LocalDate date, 
        UUID sessionTypeId
    );
    
    /**
     * Check if a specific slot is available
     * Used for pre-booking validation
     */
    boolean isSlotAvailable(
        UUID doctorId, 
        LocalDateTime startTime, 
        Duration duration
    );
    
    /**
     * Calculate next available slot after a given time
     * Used for "next available" features
     */
    Optional<AvailableSlot> getNextAvailableSlot(
        UUID doctorId, 
        LocalDateTime afterTime, 
        UUID sessionTypeId
    );
}

@Service
public class AppointmentConflictService {
    
    /**
     * Validate appointment booking before creating
     * Returns validation result with error messages if conflicts found
     */
    ValidationResult validateAppointmentBooking(
        AppointmentBookingRequest request
    );
    
    /**
     * Check for overlapping appointments
     */
    boolean hasOverlappingAppointments(
        UUID doctorId, 
        LocalDateTime startTime, 
        LocalDateTime endTime, 
        UUID excludeAppointmentId
    );
    
    /**
     * Check if booking is during doctor's time off
     */
    boolean isDuringTimeOff(
        UUID doctorId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );
}
```

**10. Event-Driven Slot Updates**:

```java
// Publish events for slot changes
@KafkaListener(topics = "healthcare.appointment.created")
public void handleAppointmentCreated(AppointmentCreatedEvent event) {
    // Invalidate slot cache for this doctor/date
    cacheService.evictSlots(event.getDoctorId(), event.getDate());
    // Recalculate and publish updated slots
    publishSlotAvailabilityUpdate(event.getDoctorId(), event.getDate());
}

@KafkaListener(topics = "healthcare.appointment.cancelled")
public void handleAppointmentCancelled(AppointmentCancelledEvent event) {
    // Restore slot availability
    cacheService.evictSlots(event.getDoctorId(), event.getDate());
    // Recalculate and publish updated slots
    publishSlotAvailabilityUpdate(event.getDoctorId(), event.getDate());
}
```

**11. Testing Requirements**:

- **Unit Tests**: Slot calculation logic, conflict detection
- **Integration Tests**: End-to-end booking flow, cancellation flow
- **Concurrency Tests**: Multiple users booking same slot simultaneously
- **Edge Cases**: Overlapping sessions, time-off boundaries, timezone handling
- **Performance Tests**: Slot calculation for busy doctors with many appointments

**12. Monitoring & Alerts**:

- Track booking success/failure rates
- Monitor slot calculation performance
- Alert on high conflict rates
- Track cancellation rates and reasons

#### 2.4 Session Service Simplification

**Current**: session-service with session types and offerings

**Optimized**: Keep as single service but simplify:

```
domains/healthcare/
├── session-service/
│   ├── SessionTypeController
│   └── SessionOfferingController
```

### Phase 3: Data Model & Multi-Tenancy Strategy

#### 3.1 Domain ID Integration

- All healthcare services use `domain_id = 'healthcare'`
- Leverage Nexus domain isolation pattern
- Services automatically filter by domain_id from JWT claims

#### 3.2 Tenant ID Strategy (Hierarchical Tenant Model - RECOMMENDED)

**Nexus Already Supports**: Tenant hierarchy with `parent_tenant_id` and `TenantType` enum (APP, SELLER, BRANCH)

**Healthcare Tenant Hierarchy**:

```
APP (Healthcare Organization - e.g., "Tiny Steps Healthcare")
├── BRANCH (Clinic Mumbai) - parent_tenant_id = APP
├── BRANCH (Clinic Delhi) - parent_tenant_id = APP
└── BRANCH (Clinic Bangalore) - parent_tenant_id = APP
```

**Implementation**:

- **APP Tenant**: Healthcare organization
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Created when organization registers
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - `parent_tenant_id = NULL`
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - `type = 'APP'`
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Organization admins belong to APP tenant

- **BRANCH Tenant**: Individual clinic/branch
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Created for each physical location
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - `parent_tenant_id = APP tenant ID`
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - `type = 'BRANCH'`
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Branch staff belong to BRANCH tenant
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - All healthcare data stored with `tenant_id = BRANCH tenant ID`

**Benefits**:

- Leverages existing Nexus `TenantHierarchyService` (no custom code needed)
- Organization admins can query across all branches using hierarchy expansion
- Branch staff only see their branch data (automatic isolation)
- No need for separate `branch_id` field
- Follows Nexus patterns already proven in ecommerce domain

**Query Pattern**:

```java
// For APP tenant admin - automatically includes all branches
List<UUID> tenantIds = tenantHierarchyService.getTenantHierarchy(appTenantId);
// Returns: [appTenantId, branch1Id, branch2Id, branch3Id]

// For BRANCH tenant staff - only their branch
List<UUID> tenantIds = tenantHierarchyService.getTenantHierarchy(branchTenantId);
// Returns: [branchTenantId] (no children)

// Use in repository queries
List<Appointment> appointments = appointmentRepository.findByTenantIdIn(tenantIds);
```

**Database Schema**:

- All healthcare entities use `tenant_id` (points to BRANCH tenant)
- Use `TenantHierarchyService.getTenantHierarchy()` to expand queries for APP tenants
- No need for separate `branch_id` column - tenant hierarchy handles it

#### 3.3 Database Schema Patterns

- All entities include: `id`, `domain_id`, `tenant_id`, `created_at`, `updated_at`, `deleted` (soft delete)
- Use Flyway migrations in each service
- Follow Nexus naming conventions

### Phase 4: Shared Library Utilization

#### 4.1 Required Dependencies (per service)

```xml
<dependencies>
    <!-- Nexus Shared Libraries -->
    <dependency>
        <groupId>com.nexus.libs</groupId>
        <artifactId>jwt-validation-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.nexus.libs</groupId>
        <artifactId>http-client-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.nexus.libs</groupId>
        <artifactId>tenant-context-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.nexus.libs</groupId>
        <artifactId>standard-response-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>com.nexus.libs</groupId>
        <artifactId>custom-error-starter</artifactId>
    </dependency>
</dependencies>
```

#### 4.2 Service-to-Service Communication

- Use `http-client-starter` for all inter-service calls
- Automatic JWT propagation via `jwt-validation-starter`
- Circuit breaker and retry patterns built-in

### Phase 5: Implementation Structure

#### 5.1 Directory Structure

```
nexus/
├── domains/
│   └── healthcare/
│       ├── doctor-service/
│       ├── patient-service/
│       ├── appointment-service/
│       ├── session-service/
│       ├── consultation-service/
│       ├── notification-service/
│       └── report-service/
├── shared/ (existing)
│   ├── iam/
│   ├── user-profile/
│   ├── address/
│   ├── payment/
│   └── gateway/
└── libs/ (existing)
```

#### 5.2 Service Template Pattern

Each healthcare service follows this structure:

```
service-name/
├── pom.xml (inherits from domains/healthcare parent)
├── src/main/java/com/nexus/domains/healthcare/{service}/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   ├── dto/
│   └── config/
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
└── README.md
```

### Phase 6: Configuration & Routing

#### 6.1 Gateway Route Configuration

Add healthcare routes to `shared/gateway`:

```yaml
routes:
 - id: healthcare-doctor-service
    uri: http://doctor-service:8080
    predicates:
   - Path=/api/v1/healthcare/doctors/**
  
 - id: healthcare-patient-service
    uri: http://patient-service:8080
    predicates:
   - Path=/api/v1/healthcare/patients/**
```

#### 6.2 Config Server Configuration

Add service configs to `config-repo/`:

```
config-repo/
├── doctor-service/
│   └── application.yml
├── patient-service/
│   └── application.yml
└── ...
```

### Phase 7: Migration Strategy

#### 7.1 Code Migration Approach

1. **Extract Entities**: Move JPA entities, adapt to Nexus patterns (add domain_id)
2. **Extract DTOs**: Adapt DTOs to Nexus standard response format
3. **Extract Services**: Move business logic, update to use shared services
4. **Extract Controllers**: Update endpoints, add domain context
5. **Database Migrations**: Create Flyway migrations with domain_id support

#### 7.2 Shared Service Integration Points

- **IAM**: Remove auth logic, use shared/iam endpoints
- **User Profile**: Use shared/user-profile, extend for doctor/patient specifics
- **Address**: Use shared/address, remove duplicate address service
- **Payment**: Use shared/payment, extend for healthcare payment types

### Phase 8: Frontend Integration

#### 8.1 API Client Updates

- Update frontend to use Nexus gateway routes (`/api/v1/healthcare/*`)
- Use domain context in API calls
- Leverage shared API client patterns from `client/packages/api-client`

#### 8.2 Multi-Domain Support

- Frontend can support multiple domains (ecommerce + healthcare)
- Domain switching via JWT refresh with new domain_id
- Shared UI components from `client/packages/components`

## Key Optimizations

### 1. Service Count Reduction

- **Before**: 13+ services
- **After**: 7 domain services + 5 shared services = 12 total
- **Benefit**: Reduced operational overhead, better resource utilization

### 2. Code Reuse

- Leverage 5 shared services (IAM, User Profile, Address, Payment, Gateway)
- Use 5 shared libraries (JWT, HTTP client, tenant context, standard response, custom error)
- **Benefit**: Less code to maintain, consistent patterns

### 3. Multi-Domain Support

- Healthcare domain isolated via domain_id
- Users can belong to multiple domains
- **Benefit**: Platform scalability, cross-domain user experience

### 4. Standardized Patterns

- All services follow Nexus conventions
- Consistent error handling, responses, authentication
- **Benefit**: Easier onboarding, predictable behavior

## Implementation Priorities

### Phase 1 (Weeks 1-2): Foundation

- Set up healthcare domain structure
- Create service templates
- Configure gateway routes
- Set up config server entries

### Phase 2 (Weeks 3-4): Core Services

- doctor-service (with specializations)
- patient-service (with medical records)
- appointment-service (combined scheduling)

### Phase 3 (Weeks 5-6): Supporting Services

- session-service
- consultation-service
- notification-service
- report-service

### Phase 4 (Weeks 7-8): Integration & Testing

- Integrate with shared services
- End-to-end testing
- Frontend integration
- Performance optimization

## Success Metrics

- **Service Reduction**: 13+ → 7 domain services
- **Code Reuse**: 40%+ code reduction via shared services
- **Consistency**: 100% services using Nexus patterns
- **Multi-Domain**: Users can access healthcare + ecommerce
- **Performance**: <2s API response times
- **Scalability**: Support 100+ tenants (branches/clinics)

## Risks & Mitigations

### Risk 1: Data Migration Complexity

- **Mitigation**: Phased migration, maintain backward compatibility during transition

### Risk 2: Service Coupling

- **Mitigation**: Clear service boundaries, event-driven communication where needed

### Risk 3: Shared Service Limitations

- **Mitigation**: Extend shared services with domain-specific features, use adapter pattern

## Advanced Optimization & Scalability Strategies

### 5. CQRS (Command Query Responsibility Segregation)

**Implementation**:

- Separate read and write models for high-traffic entities (appointments, patients, doctors)
- Read models optimized for queries (denormalized, indexed)
- Write models optimized for transactions (normalized, ACID)
- Event-driven synchronization between read/write models

**Benefits**:

- Independent scaling of read/write operations
- Optimized query performance (no joins on read side)
- Better handling of complex reporting queries

**Services to Apply**:

- `appointment-service`: Separate read model for calendar views, statistics
- `patient-service`: Read model for patient search, medical history views
- `doctor-service`: Read model for doctor listings, availability queries

### 6. Event-Driven Architecture Enhancement

**Current**: Basic Kafka integration in Nexus

**Enhancement**: Comprehensive event sourcing for audit trails and state reconstruction

**Event Topics** (following Nexus pattern: `healthcare.{entity}.{action}`):

```
healthcare.appointment.created
healthcare.appointment.confirmed
healthcare.appointment.cancelled
healthcare.patient.registered
healthcare.patient.updated
healthcare.doctor.verified
healthcare.consultation.completed
healthcare.payment.processed
```

**Benefits**:

- Complete audit trail for compliance (HIPAA)
- Event replay for data recovery
- Real-time analytics via event streams
- Loose coupling between services

### 7. Advanced Caching Strategy

**Multi-Layer Caching**:

1. **L1 Cache (In-Memory)**: Frequently accessed data (doctor profiles, session types)

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Use Spring Cache with Caffeine
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - TTL: 5-15 minutes
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Size: 10,000 entries per service

2. **L2 Cache (Redis)**: Shared cache across service instances

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Search results (1 hour TTL)
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Patient lists (30 minutes TTL)
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Appointment calendars (15 minutes TTL)
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Statistics/aggregations (5 minutes TTL)

3. **L3 Cache (Database Materialized Views)**: Pre-computed aggregations

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Daily appointment statistics
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Doctor availability summaries
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                - Patient count by branch

**Cache Invalidation Strategy**:

- Write-through for critical data (appointments, payments)
- Write-behind for non-critical data (statistics, reports)
- Event-driven invalidation via Kafka

### 8. Database Optimization

**Read Replicas**:

- Primary database for writes (ACID transactions)
- Read replicas for queries (scalable reads)
- Automatic failover with connection pooling

**Database Sharding** (Future):

- Shard by tenant_id for large-scale multi-tenancy
- Horizontal partitioning for patient/appointment tables
- Cross-shard queries via API gateway aggregation

**Indexing Strategy**:

- Composite indexes on (domain_id, tenant_id, entity_id)
- Full-text indexes for search (PostgreSQL tsvector)
- Partial indexes for soft-deleted records
- Covering indexes for common query patterns

### 9. Search Service Enhancement

**Current**: PostgreSQL full-text search with Redis caching (as seen in Nexus ecommerce)

**Enhancement**: Hybrid search architecture

**Option A: Elasticsearch Integration** (Recommended for scale):

- Elasticsearch for advanced search (fuzzy, faceted, geo)
- PostgreSQL for transactional data
- Sync via Kafka events (eventual consistency)
- Fallback to PostgreSQL if Elasticsearch unavailable

**Option B: Enhanced PostgreSQL** (Start here):

- Materialized views for search indexes
- GIN indexes for full-text search
- JSONB indexes for metadata search
- Refresh strategy via scheduled jobs or events

**Search Features**:

- Patient search: name, phone, email, medical record number
- Doctor search: name, specialization, location, availability
- Appointment search: date range, status, doctor, patient
- Medical history search: conditions, medications, allergies

### 10. API Composition & BFF (Backend for Frontend)

**Problem**: Frontend makes multiple API calls for dashboard views

**Solution**: API Composition Service

**New Service**: `healthcare-api-composition-service`

- Aggregates data from multiple services
- Single endpoint for complex views (dashboard, patient detail, appointment calendar)
- Reduces frontend complexity and network calls
- Caches composed responses

**Example Endpoints**:

```
GET /api/v1/healthcare/dashboard/summary
  → Aggregates: appointments, patients, doctors, revenue
  
GET /api/v1/healthcare/patients/{id}/complete
  → Aggregates: patient, addresses, medical history, appointments, payments
```

### 11. Real-Time Capabilities

**WebSocket Integration**:

- Real-time appointment updates
- Live availability changes
- Instant notifications
- Collaborative features (multiple users viewing same appointment)

**Server-Sent Events (SSE)**:

- Appointment reminders
- Status change notifications
- Report generation completion

**Implementation**:

- Use Spring WebFlux for reactive streams
- Gateway WebSocket proxy configuration
- Redis pub/sub for cross-instance messaging

### 12. GraphQL API Layer (Optional)

**Use Case**: Flexible queries for mobile apps and third-party integrations

**Implementation**:

- GraphQL gateway service
- Resolvers that call underlying REST services
- Caching at resolver level
- Rate limiting per query complexity

**Benefits**:

- Reduced over-fetching
- Single endpoint for all queries
- Self-documenting API schema

### 13. Materialized Views for Analytics

**Pre-computed Aggregations**:

- Daily appointment statistics per branch
- Doctor utilization rates
- Patient visit frequency
- Revenue by service type
- Peak hours analysis

**Refresh Strategy**:

- Scheduled refresh (nightly for historical data)
- Event-driven refresh (real-time for current day)
- Incremental updates via Kafka consumers

### 14. Service Mesh Integration (Future)

**Benefits**:

- Advanced traffic management (A/B testing, canary deployments)
- Automatic retry, circuit breaking, timeouts
- Distributed tracing (Jaeger, Zipkin)
- mTLS for service-to-service communication

**Tools**: Istio, Linkerd, or Consul Connect

### 15. Horizontal Scaling Strategy

**Stateless Services**:

- All services designed as stateless
- Session data in Redis (not in-memory)
- JWT tokens for authentication (no server-side sessions)

**Load Balancing**:

- Gateway-level load balancing
- Service-level load balancing (Kubernetes Service)
- Database connection pooling (HikariCP)

**Auto-Scaling**:

- Kubernetes HPA (Horizontal Pod Autoscaler)
- Based on CPU, memory, or custom metrics (request rate)
- Scale down during off-peak hours

### 16. Database Connection Pooling

**Configuration**:

- HikariCP with optimized pool sizes
- Separate pools for read/write operations
- Connection timeout and retry logic
- Monitoring connection pool metrics

**Pool Sizing Formula**:

```
connections = ((core_count * 2) + effective_spindle_count)
```

### 17. Async Processing for Heavy Operations

**Use Cases**:

- Report generation (Excel, PDF exports)
- Bulk patient imports
- Appointment reminders (batch processing)
- Analytics calculations

**Implementation**:

- Kafka for job queuing
- Separate worker services for processing
- Progress tracking via Redis
- Webhook/SSE for completion notifications

### 18. API Rate Limiting

**Implementation**:

- Gateway-level rate limiting (Redis-based)
- Per-user, per-tenant, per-endpoint limits
- Sliding window algorithm
- Graceful degradation (429 responses)

**Limits**:

- Public endpoints: 100 req/min per IP
- Authenticated: 1000 req/min per user
- Admin endpoints: 5000 req/min per user

### 19. Circuit Breaker Pattern

**Already in Nexus**: http-client-starter includes Resilience4j

**Enhancement**:

- Configure circuit breakers for all inter-service calls
- Fallback responses for non-critical operations
- Health check integration

**Example**:

- If patient-service is down, appointment-service returns cached patient data
- If payment-service is down, allow appointment booking with "payment pending" status

### 20. Distributed Tracing

**Implementation**:

- OpenTelemetry integration
- Trace IDs in all logs
- Correlation IDs in API responses
- Integration with monitoring tools (Grafana, Prometheus)

**Benefits**:

- End-to-end request tracking
- Performance bottleneck identification
- Debugging distributed systems

## Scalability Metrics & Targets

### Performance Targets

- **API Response Time**: <200ms (p95), <500ms (p99)
- **Database Query Time**: <100ms (p95)
- **Cache Hit Rate**: >80% for read operations
- **Throughput**: 10,000+ requests/second per service instance
- **Concurrent Users**: 50,000+ simultaneous users

### Scalability Targets

- **Tenants (Branches)**: 1,000+ tenants
- **Patients**: 10M+ patient records
- **Appointments**: 1M+ appointments/month
- **Doctors**: 10,000+ doctors
- **Database Size**: 100GB+ with read replicas

### Availability Targets

- **Uptime**: 99.9% (8.76 hours downtime/year)
- **RTO (Recovery Time Objective)**: <15 minutes
- **RPO (Recovery Point Objective)**: <5 minutes

## Next Steps

1. Confirm service decomposition approach
2. Validate tenant/branch mapping strategy
3. Review shared service extension points
4. Prioritize optimization strategies (start with caching, then CQRS, then advanced features)
5. Create detailed service specifications
6. Begin Phase 1 implementation

### To-dos

- [ ] Analyze Tiny Steps service boundaries and identify optimization opportunities
- [ ] Map Tiny Steps services to Nexus shared services (IAM, User Profile, Address, Payment)
- [ ] Design healthcare domain service structure under domains/healthcare/
- [ ] Define tenant/branch mapping strategy for multi-tenancy in Nexus
- [ ] Create service templates following Nexus patterns (pom.xml, structure, configs)
- [ ] Create detailed migration plan from Tiny Steps to Nexus healthcare domain