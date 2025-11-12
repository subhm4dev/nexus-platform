-- Session Service Schema Migration
-- Creates tables for session types and offerings

-- Session Types table
CREATE TABLE session_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL, -- Session duration in minutes
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Session Offerings table
CREATE TABLE session_offerings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_type_id UUID NOT NULL REFERENCES session_types(id) ON DELETE CASCADE,
    doctor_id UUID, -- Optional: specific doctor offering
    price DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for session_types table
CREATE INDEX idx_session_types_tenant ON session_types(tenant_id, domain_id);
CREATE INDEX idx_session_types_active ON session_types(is_active);

-- Indexes for session_offerings table
CREATE INDEX idx_session_offerings_session_type ON session_offerings(session_type_id);
CREATE INDEX idx_session_offerings_doctor ON session_offerings(doctor_id);
CREATE INDEX idx_session_offerings_tenant ON session_offerings(tenant_id, domain_id);
CREATE INDEX idx_session_offerings_active ON session_offerings(is_active);

-- Comments
COMMENT ON TABLE session_types IS 'Session types with duration (e.g., Consultation: 30 min, Therapy: 45 min)';
COMMENT ON TABLE session_offerings IS 'Session pricing and availability';

