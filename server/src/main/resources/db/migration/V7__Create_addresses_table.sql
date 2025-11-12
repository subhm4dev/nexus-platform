-- Addresses Migration
-- Creates the addresses table for storing user shipping addresses
-- Consolidated migration: Includes domain_id from the start

CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, -- References user_accounts.id in Identity service (no FK constraint - different service)
    tenant_id UUID NOT NULL, -- Multi-tenant isolation
    domain_id UUID NOT NULL, -- References domains.id - the business domain this address belongs to
    line1 VARCHAR(255) NOT NULL,
    line2 VARCHAR(255), -- Optional second line (apartment, suite, etc.)
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100), -- Optional state/province
    postcode VARCHAR(20) NOT NULL,
    country VARCHAR(2) NOT NULL, -- ISO 3166-1 alpha-2 country code (e.g., "US", "IN")
    label VARCHAR(50), -- Optional label (e.g., "Home", "Office", "Warehouse")
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE, -- Soft delete flag
    deleted_at TIMESTAMP, -- Timestamp when address was soft deleted
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Partial unique index to prevent duplicate addresses (only for active addresses)
-- This allows same address to exist once per user, but only if not deleted
CREATE UNIQUE INDEX idx_addresses_unique_active 
    ON addresses(user_id, domain_id, tenant_id, line1, city, postcode, country) 
    WHERE deleted = FALSE;

-- Index for faster lookups by user_id
CREATE INDEX idx_addresses_user_id ON addresses(user_id);

-- Index for faster lookups by tenant_id
CREATE INDEX idx_addresses_tenant_id ON addresses(tenant_id);

-- Index for filtering by deleted status
CREATE INDEX idx_addresses_deleted ON addresses(deleted);

-- Composite index for common queries (user + tenant + deleted)
CREATE INDEX idx_addresses_user_tenant_deleted ON addresses(user_id, tenant_id, deleted);

-- Domain-related indexes
CREATE INDEX idx_addresses_domain_id ON addresses(domain_id);
CREATE INDEX idx_addresses_domain_tenant ON addresses(domain_id, tenant_id);
CREATE INDEX idx_addresses_user_domain_tenant ON addresses(user_id, domain_id, tenant_id);

-- Comments for documentation
COMMENT ON TABLE addresses IS 'Stores shipping addresses for users. Supports multi-tenant isolation and soft delete.';
COMMENT ON COLUMN addresses.user_id IS 'References user_accounts.id in Identity service (no FK - different service/database)';
COMMENT ON COLUMN addresses.tenant_id IS 'Tenant ID for multi-tenant data isolation';
COMMENT ON COLUMN addresses.domain_id IS 'References domains.id - the business domain this address belongs to';
COMMENT ON COLUMN addresses.line1 IS 'First line of address (e.g., "123 Main Street")';
COMMENT ON COLUMN addresses.line2 IS 'Second line of address (optional, e.g., "Apartment 4B")';
COMMENT ON COLUMN addresses.city IS 'City name';
COMMENT ON COLUMN addresses.state IS 'State or province (optional)';
COMMENT ON COLUMN addresses.postcode IS 'Postal or ZIP code';
COMMENT ON COLUMN addresses.country IS 'Country code (ISO 3166-1 alpha-2, e.g., "US", "IN")';
COMMENT ON COLUMN addresses.label IS 'Address label for identification (e.g., "Home", "Office", "Warehouse")';
COMMENT ON COLUMN addresses.is_default IS 'Whether this is the default address for the user';
COMMENT ON COLUMN addresses.deleted IS 'Soft delete flag - when true, address is considered deleted but retained for audit';
COMMENT ON COLUMN addresses.deleted_at IS 'Timestamp when address was soft deleted (null if active)';
