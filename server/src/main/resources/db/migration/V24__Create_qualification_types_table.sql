-- Qualification Types Table Migration
-- Creates master data table for qualification types (e.g., MBBS, MD, PhD)

-- Qualification Types table (master data - shared across tenants)
CREATE TABLE qualification_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE, -- e.g., "MBBS"
    name VARCHAR(255) NOT NULL, -- e.g., "Bachelor of Medicine, Bachelor of Surgery"
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for qualification_types table
CREATE INDEX idx_qualification_types_code ON qualification_types(code);
CREATE INDEX idx_qualification_types_name ON qualification_types(name);

-- Comments
COMMENT ON TABLE qualification_types IS 'Master data for medical qualification types (shared across tenants)';

