# Domain ID Implementation Guide

## Table of Contents
1. [Overview](#overview)
2. [Problem Statement](#problem-statement)
3. [Solution Architecture](#solution-architecture)
4. [Database Schema Changes](#database-schema-changes)
5. [Code Changes](#code-changes)
6. [Migration Strategy](#migration-strategy)
7. [Multi-Domain User Support](#multi-domain-user-support)
8. [How It Works](#how-it-works)
9. [Examples](#examples)
10. [Testing Checklist](#testing-checklist)
11. [Future Considerations](#future-considerations)

---

## Overview

This document describes the implementation of **domain isolation** in the Nexus Platform. The platform supports multiple business domains (ecommerce, hospital, food-delivery, travel, etc.) while maintaining proper data isolation and allowing users to belong to multiple domains.

### Key Concepts

- **Domain**: A business domain (e.g., `ecommerce`, `hospital`, `food-delivery`, `travel`)
- **Tenant**: A business entity within a domain (e.g., a seller in ecommerce, a lab provider in hospital)
- **User**: Can belong to multiple domains simultaneously

---

## Problem Statement

### Original Issue

The platform was designed with `tenant_id` for multi-tenancy, but `tenant_id` was intended for **business entity isolation** (sellers, providers), not **domain isolation** (ecommerce vs. hospital).

### Challenges

1. **Data Isolation**: How to separate ecommerce data from hospital data in shared services?
2. **User Multi-Domain**: How to allow users to be part of multiple domains?
3. **Query Filtering**: How to ensure queries filter by both domain and tenant?
4. **JWT Claims**: How to include domain context in authentication tokens?

### Example Scenario

- User "John" shops on ecommerce platform → `tenant_id: marketplace, domain_id: ecommerce`
- Same user "John" books lab tests on hospital platform → `tenant_id: lab-provider, domain_id: hospital`
- Both should be accessible with the same user account, but data must be isolated

---

## Solution Architecture

### Design Decision: Domain ID + Tenant ID

We use a **two-level isolation** strategy:

1. **Domain Level** (`domain_id`): Separates different business domains
2. **Tenant Level** (`tenant_id`): Separates business entities within a domain

### Data Isolation Strategy

```
┌─────────────────────────────────────────────────────────┐
│                    Platform Level                       │
├─────────────────────────────────────────────────────────┤
│  Domain: ecommerce    │  Domain: hospital              │
│  ┌─────────────────┐  │  ┌─────────────────┐           │
│  │ Tenant: Seller1 │  │  │ Tenant: Lab1    │           │
│  │ Tenant: Seller2 │  │  │ Tenant: Lab2    │           │
│  │ Tenant: Market  │  │  │ Tenant: Market  │           │
│  └─────────────────┘  │  └─────────────────┘           │
└─────────────────────────────────────────────────────────┘
```

### User-Domain Relationship

Users can belong to **multiple domains** via the `user_domains` junction table:

```
User "John"
├── Domain: ecommerce, Tenant: marketplace
├── Domain: hospital, Tenant: lab-provider-1
└── Domain: food-delivery, Tenant: restaurant-xyz
```

---

## Database Schema Changes

### 1. Tenants Table

**Migration**: `V3__Add_domain_id_to_tenants.sql`

```sql
ALTER TABLE tenants ADD COLUMN domain_id VARCHAR(50);

-- Auto-generate based on tenant name patterns
UPDATE tenants 
SET domain_id = CASE 
    WHEN LOWER(name) LIKE '%hospital%' THEN 'hospital'
    WHEN LOWER(name) LIKE '%food%' THEN 'food-delivery'
    WHEN LOWER(name) LIKE '%travel%' THEN 'travel'
    ELSE 'ecommerce'
END;

ALTER TABLE tenants ALTER COLUMN domain_id SET NOT NULL;
CREATE INDEX idx_tenants_domain ON tenants(domain_id);
```

**Changes**:
- Added `domain_id` column (required, indexed)
- Auto-populated for existing tenants
- Defaults to `'ecommerce'` if pattern doesn't match

### 2. User Domains Table (NEW)

**Migration**: `V5__Create_user_domains_table.sql`

```sql
CREATE TABLE user_domains (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES user_accounts(id),
    domain_id VARCHAR(50) NOT NULL,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_domain_tenant UNIQUE (user_id, domain_id, tenant_id)
);
```

**Purpose**: Tracks which domains a user belongs to (many-to-many relationship)

**Key Features**:
- One user can have multiple entries (one per domain)
- Unique constraint on `(user_id, domain_id, tenant_id)`
- Auto-populated for existing users based on their tenant's domain

### 3. Addresses Table

**Migration**: `V2__Add_domain_id_to_addresses.sql`

```sql
ALTER TABLE addresses ADD COLUMN domain_id VARCHAR(50);

-- Auto-generate by looking up tenant's domain
UPDATE addresses a
SET domain_id = (
    SELECT t.domain_id FROM tenants t WHERE t.id = a.tenant_id
);

ALTER TABLE addresses ALTER COLUMN domain_id SET NOT NULL;

-- Update unique constraint to include domain_id
CREATE UNIQUE INDEX idx_addresses_unique_active 
    ON addresses(user_id, domain_id, tenant_id, line1, city, postcode, country) 
    WHERE deleted = false;
```

**Changes**:
- Added `domain_id` column
- Updated unique constraint to include `domain_id`
- All queries now filter by `(user_id, domain_id, tenant_id)`

### 4. User Profiles Table

**Migration**: `V2__Add_domain_id_to_user_profiles.sql`

```sql
-- Drop old unique constraint (user_id only)
ALTER TABLE user_profiles DROP CONSTRAINT user_profiles_user_id_unique;

-- Add domain_id
ALTER TABLE user_profiles ADD COLUMN domain_id VARCHAR(50);

-- Auto-generate from user's tenant
UPDATE user_profiles up
SET domain_id = (
    SELECT t.domain_id 
    FROM user_accounts ua
    INNER JOIN tenants t ON ua.tenant_id = t.id
    WHERE ua.id = up.user_id
);

-- New unique constraint: one profile per user per domain
CREATE UNIQUE INDEX idx_user_profiles_user_domain_unique 
    ON user_profiles(user_id, domain_id);
```

**Key Change**: Users can now have **multiple profiles** (one per domain)

### 5. Payments & Payment Methods Tables

**Migration**: `V2__Add_domain_id_to_payments.sql`

```sql
-- Payments table
ALTER TABLE payments ADD COLUMN domain_id VARCHAR(50);
UPDATE payments p
SET domain_id = (SELECT t.domain_id FROM tenants t WHERE t.id = p.tenant_id);
ALTER TABLE payments ALTER COLUMN domain_id SET NOT NULL;

-- Payment methods table
ALTER TABLE payment_methods ADD COLUMN domain_id VARCHAR(50);
UPDATE payment_methods pm
SET domain_id = (SELECT t.domain_id FROM tenants t WHERE t.id = pm.tenant_id);
ALTER TABLE payment_methods ALTER COLUMN domain_id SET NOT NULL;
```

**Changes**: Both tables now include `domain_id` for domain isolation

---

## Code Changes

### 1. IAM Service (Identity & Access Management)

#### Entity Changes

**Tenant.java**
```java
@Column(nullable = false, name = "domain_id", length = 50)
private String domainId;
```

**UserDomain.java** (NEW)
```java
@Entity
@Table(name = "user_domains")
public class UserDomain {
    private UUID userId;
    private String domainId;
    private UUID tenantId;
}
```

#### Service Changes

**AuthServiceImpl.java**
- Creates `UserDomain` entry when user registers
- Associates user with tenant's domain automatically

**JwtService.java**
- Includes `domainId` in JWT claims
- Extracts `domainId` from tenant when generating token

#### Repository Changes

**UserDomainRepository.java** (NEW)
- Methods to find user's domains
- Check if user belongs to a domain
- Find users in a specific domain

### 2. JWT Validation Library

**JwtTokenParser.java**
```java
public static String extractDomainId(JWTClaimsSet claims) {
    // Extracts domainId from JWT claims
    // Defaults to 'ecommerce' for backward compatibility
}
```

**BlockingJwtValidationService.java** & **ReactiveJwtValidationService.java**
- Added `extractDomainId()` method
- Both blocking and reactive implementations updated

### 3. Address Service

**Entity**: `Address.java`
- Added `domainId` field

**Repository**: `AddressRepository.java`
- All methods now include `domainId` parameter
- Queries filter by `(userId, domainId, tenantId)`

**Service**: `AddressServiceImpl.java`
- All methods updated to accept and use `domainId`
- Domain + tenant validation added

**Controller**: `AddressController.java`
- Extracts `domainId` from JWT
- Passes to service methods

**Security**: `JwtAuthenticationToken.java` & `JwtAuthenticationFilter.java`
- Added `domainId` field
- Extracts from JWT claims

### 4. User Profile Service

**Entity**: `UserProfile.java`
- Added `domainId` field
- Removed unique constraint on `user_id` alone
- New unique constraint on `(user_id, domain_id)`

**Repository**: `UserProfileRepository.java`
- Methods updated to include `domainId`

**Service**: `UserProfileServiceImpl.java`
- Supports multiple profiles per user (one per domain)

**Controller & Security**: Similar changes as Address service

### 5. Payment Service

**Entities**: `Payment.java` & `PaymentMethod.java`
- Added `domainId` field

**Repositories**: Updated to include `domainId` in queries

**Service**: `PaymentServiceImpl.java`
- All methods updated to accept and use `domainId`

**Controller & Security**: Similar changes as Address service

---

## Migration Strategy

### Migration Order

1. **V1**: Create initial schema (tenants, user_accounts, etc.)
2. **V2**: Create default marketplace tenant (without domain_id)
3. **V3**: Add domain_id to tenants, auto-populate based on name patterns
4. **V4**: Safety check for default tenant
5. **V5**: Create user_domains table, auto-populate for existing users
6. **V2 (Address)**: Add domain_id to addresses, auto-populate from tenant lookup
7. **V2 (User Profile)**: Add domain_id to user_profiles, auto-populate from user's tenant
8. **V2 (Payment)**: Add domain_id to payments and payment_methods

### Auto-Population Strategy

All migrations use **intelligent auto-population**:

1. **Tenants**: Pattern matching on tenant name
2. **Addresses**: Lookup tenant's domain_id
3. **User Profiles**: Lookup user's tenant, then tenant's domain_id
4. **Payments**: Lookup tenant's domain_id
5. **User Domains**: Based on user's tenant's domain_id

### Backward Compatibility

- All migrations default to `'ecommerce'` if domain cannot be determined
- JWT validation defaults to `'ecommerce'` if `domainId` claim is missing
- Existing tokens without `domainId` will still work (with default)

---

## Multi-Domain User Support

### Architecture

Users can belong to **multiple domains** via the `user_domains` junction table:

```
┌─────────────┐
│ User: John  │
└──────┬──────┘
       │
       ├─── Domain: ecommerce, Tenant: marketplace
       ├─── Domain: hospital, Tenant: lab-provider-1
       └─── Domain: food-delivery, Tenant: restaurant-xyz
```

### How It Works

1. **Registration**: User is automatically added to their tenant's domain
2. **Domain Addition**: Can be added to additional domains via `UserDomain` entries
3. **JWT Context**: JWT contains `domainId` indicating current domain context
4. **Data Isolation**: All queries filter by `domainId` + `tenantId`

### Adding User to Additional Domain

```java
// Example: Add user to hospital domain
UserDomain hospitalDomain = UserDomain.builder()
    .userId(userId)
    .domainId("hospital")
    .tenantId(hospitalTenantId)
    .build();
userDomainRepository.save(hospitalDomain);
```

### Querying User's Domains

```java
// Get all domains for a user
List<UserDomain> userDomains = userDomainRepository.findByUserId(userId);

// Check if user belongs to a domain
boolean belongsToHospital = userDomainRepository
    .existsByUserIdAndDomainId(userId, "hospital");
```

---

## How It Works

### Authentication Flow

1. **User Logs In** → IAM service generates JWT
2. **JWT Contains**:
   - `userId`: User identifier
   - `tenantId`: Current tenant
   - `domainId`: Current domain (from tenant)
   - `roles`: User roles

3. **Request Flow**:
   ```
   Client → Gateway → Service
   Gateway validates JWT and extracts claims
   Service receives request with JWT
   Service extracts domainId from JWT
   Service queries database with (userId, domainId, tenantId)
   ```

### Data Query Pattern

**Before** (tenant only):
```java
List<Address> addresses = addressRepository
    .findByUserIdAndTenantId(userId, tenantId);
```

**After** (domain + tenant):
```java
List<Address> addresses = addressRepository
    .findByUserIdAndDomainIdAndTenantId(userId, domainId, tenantId);
```

### Domain Context in JWT

```json
{
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "tenantId": "00000000-0000-0000-0000-000000000000",
  "domainId": "ecommerce",
  "roles": ["CUSTOMER"]
}
```

---

## Examples

### Example 1: User Shopping on Ecommerce

```
1. User logs in → JWT: {domainId: "ecommerce", tenantId: "marketplace"}
2. User adds address → Stored with domainId: "ecommerce"
3. User creates profile → Stored with domainId: "ecommerce"
4. User makes payment → Stored with domainId: "ecommerce"
```

### Example 2: Same User Booking Lab Test

```
1. User logs in with hospital tenant → JWT: {domainId: "hospital", tenantId: "lab-1"}
2. User adds address → Stored with domainId: "hospital" (separate from ecommerce address)
3. User creates profile → Stored with domainId: "hospital" (separate profile)
4. User makes payment → Stored with domainId: "hospital"
```

### Example 3: Querying User's Data

```java
// Get user's ecommerce addresses
List<Address> ecommerceAddresses = addressRepository
    .findByUserIdAndDomainIdAndTenantIdAndDeletedFalse(
        userId, "ecommerce", tenantId);

// Get user's hospital addresses
List<Address> hospitalAddresses = addressRepository
    .findByUserIdAndDomainIdAndTenantIdAndDeletedFalse(
        userId, "hospital", hospitalTenantId);
```

---

## Testing Checklist

### Database Migrations
- [ ] Run all migrations successfully
- [ ] Verify `domain_id` is populated for all existing records
- [ ] Verify `user_domains` table is populated
- [ ] Verify indexes are created

### IAM Service
- [ ] User registration creates `UserDomain` entry
- [ ] JWT includes `domainId` claim
- [ ] Default tenant has `domainId: "ecommerce"`

### Address Service
- [ ] Create address with domain context
- [ ] Query addresses filtered by domain
- [ ] Address isolation between domains

### User Profile Service
- [ ] Create profile in one domain
- [ ] Create separate profile in another domain
- [ ] Query profiles filtered by domain

### Payment Service
- [ ] Process payment with domain context
- [ ] Query payments filtered by domain
- [ ] Payment method isolation between domains

### JWT Validation
- [ ] Extract `domainId` from JWT
- [ ] Backward compatibility (missing `domainId` defaults to "ecommerce")
- [ ] Gateway validates and propagates `domainId`

### Multi-Domain User
- [ ] User can belong to multiple domains
- [ ] User can access data in each domain separately
- [ ] Domain switching works correctly

---

## Future Considerations

### 1. Domain Configuration

Consider creating a `domains` table for domain metadata:
```sql
CREATE TABLE domains (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(255),
    description TEXT,
    enabled BOOLEAN DEFAULT TRUE
);
```

### 2. Domain-Specific Features

Some domains may need domain-specific features:
- Hospital: Patient records, appointments
- Food-delivery: Restaurant menus, delivery zones
- Travel: Bookings, itineraries

### 3. Cross-Domain Operations

Consider how to handle operations that span domains:
- User wants to use ecommerce address for hospital appointment
- User wants to see all orders across domains

### 4. Domain Switching

Implement UI/API for users to switch between domains:
```java
POST /api/v1/auth/switch-domain
{
  "domainId": "hospital",
  "tenantId": "lab-provider-1"
}
```

### 5. Domain Permissions

Consider domain-specific permissions:
- User may have different roles in different domains
- Admin in ecommerce, Customer in hospital

### 6. Domain Analytics

Track domain-specific metrics:
- User activity per domain
- Revenue per domain
- User growth per domain

---

## Summary

### What Changed

1. **Database**: Added `domain_id` to all shared service tables
2. **JWT**: Includes `domainId` claim
3. **Services**: All queries filter by `domainId` + `tenantId`
4. **Multi-Domain**: Users can belong to multiple domains via `user_domains` table

### Benefits

- ✅ **Proper Isolation**: Data separated by domain
- ✅ **Multi-Domain Users**: Users can access multiple domains
- ✅ **Backward Compatible**: Existing data migrated automatically
- ✅ **Scalable**: Easy to add new domains

### Migration Path

1. Run database migrations (auto-populates `domain_id`)
2. Deploy updated services
3. Existing users automatically get `UserDomain` entries
4. New users automatically associated with their tenant's domain

---

## Questions?

If you have questions about the implementation, please refer to:
- Migration scripts in `shared/*/src/main/resources/db/migration/`
- Entity classes in `shared/*/src/main/java/com/ecom/*/entity/`
- Service implementations in `shared/*/src/main/java/com/ecom/*/service/impl/`

