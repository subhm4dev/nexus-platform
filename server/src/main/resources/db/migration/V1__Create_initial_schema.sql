-- IAM Service Initial Schema
-- Consolidated migration: Creates all tables, types, indexes, and constraints
-- No INSERT/UPDATE/DELETE statements - only DDL for table setup

-- Enable UUID extension (PostgreSQL 13+ has gen_random_uuid() built-in)
-- Using gen_random_uuid() which is available in PostgreSQL 13+ without extension

-- Essential enums for MVP
CREATE TYPE tenant_status AS ENUM ('ACTIVE', 'INACTIVE');
CREATE TYPE user_role AS ENUM ('CUSTOMER', 'SELLER', 'ADMIN', 'STAFF', 'DRIVER');

-- Domains table (must be created before tenants since tenants reference it)
-- Domains represent different business domains: ecommerce, hospital, food-delivery, travel, etc.
CREATE TABLE domains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(50) UNIQUE NOT NULL,  -- Human-readable code: "ecommerce", "hospital", etc.
    name VARCHAR(255) NOT NULL,         -- Display name: "E-commerce", "Hospital Management", etc.
    description TEXT,                   -- Optional description
    enabled BOOLEAN DEFAULT TRUE,       -- Can disable domains without deleting
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for code lookups (most common query)
CREATE INDEX idx_domains_code ON domains(code);
CREATE INDEX idx_domains_enabled ON domains(enabled);

-- Comments for domains
COMMENT ON TABLE domains IS 'Master table for business domains. Each domain represents a different business vertical.';
COMMENT ON COLUMN domains.code IS 'Unique human-readable code (e.g., "ecommerce", "hospital")';
COMMENT ON COLUMN domains.name IS 'Display name for the domain';
COMMENT ON COLUMN domains.enabled IS 'Whether the domain is active. Disabled domains cannot be used for new tenants/users.';

-- Core tables for authentication
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status tenant_status DEFAULT 'ACTIVE',
    domain_id UUID NOT NULL REFERENCES domains(id) ON DELETE RESTRICT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for tenants
CREATE INDEX idx_tenants_domain ON tenants(domain_id);

-- Comments for tenants
COMMENT ON COLUMN tenants.domain_id IS 'References domains.id - the business domain this tenant belongs to';

CREATE TABLE user_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255), -- Tenant-scoped uniqueness (see constraint below)
    phone VARCHAR(20), -- Tenant-scoped uniqueness (see constraint below)
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL, -- Explicit salt for salt+pepper technique
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    enabled BOOLEAN DEFAULT TRUE,
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT email_or_phone_required CHECK (email IS NOT NULL OR phone IS NOT NULL),
    -- Email/phone unique per tenant:
    -- - Customers share default tenant (00000000-0000-0000-0000-000000000000) 
    --   → email/phone effectively globally unique for customers
    -- - Sellers have their own tenants → same email/phone can exist across different seller tenants
    CONSTRAINT unique_email_tenant UNIQUE (email, tenant_id),
    CONSTRAINT unique_phone_tenant UNIQUE (phone, tenant_id)
);

CREATE INDEX idx_user_accounts_email ON user_accounts(email);
CREATE INDEX idx_user_accounts_phone ON user_accounts(phone);
CREATE INDEX idx_user_accounts_tenant ON user_accounts(tenant_id);

CREATE TABLE role_grants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    role user_role NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_role UNIQUE (user_id, role)
);

CREATE INDEX idx_role_grants_user ON role_grants(user_id);

CREATE TABLE jwk_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kid VARCHAR(50) UNIQUE NOT NULL,
    public_key_pem TEXT NOT NULL,
    private_key_pem TEXT NOT NULL,
    algorithm VARCHAR(50) DEFAULT 'RS256',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP
);

CREATE INDEX idx_jwk_keys_kid ON jwk_keys(kid);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

-- Create user_domains junction table to track which domains a user belongs to
-- A user can be part of multiple domains (e.g., ecommerce and hospital)
-- Uses UUID domain_id for referential integrity
CREATE TABLE user_domains (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES user_accounts(id) ON DELETE CASCADE,
    domain_id UUID NOT NULL REFERENCES domains(id) ON DELETE RESTRICT,
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- A user can have multiple entries for the same domain (different tenants)
    -- But typically one entry per (user_id, domain_id, tenant_id) combination
    CONSTRAINT unique_user_domain_tenant UNIQUE (user_id, domain_id, tenant_id)
);

-- Indexes for efficient lookups
CREATE INDEX idx_user_domains_user_id ON user_domains(user_id);
CREATE INDEX idx_user_domains_domain_id ON user_domains(domain_id);
CREATE INDEX idx_user_domains_tenant_id ON user_domains(tenant_id);
CREATE INDEX idx_user_domains_user_domain ON user_domains(user_id, domain_id);

-- Comments for user_domains
COMMENT ON TABLE user_domains IS 'Junction table tracking which domains a user belongs to. A user can be part of multiple domains.';
COMMENT ON COLUMN user_domains.user_id IS 'References user_accounts.id';
COMMENT ON COLUMN user_domains.domain_id IS 'References domains.id - the business domain';
COMMENT ON COLUMN user_domains.tenant_id IS 'Tenant ID within the domain (business entity: seller, provider, etc.)';
