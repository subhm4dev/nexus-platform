-- Doctor Service Schema Migration
-- Creates tables for doctors, specializations, qualifications, and awards

-- Doctors table
CREATE TABLE doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, -- References user_accounts.id in Identity service
    tenant_id UUID NOT NULL, -- Multi-tenant isolation (BRANCH tenant)
    domain_id UUID NOT NULL, -- References domains.id (healthcare domain)
    registration_number VARCHAR(100) UNIQUE,
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, VERIFIED, REJECTED
    years_of_experience INTEGER,
    consultation_fee DECIMAL(19, 2),
    bio TEXT,
    profile_image_url VARCHAR(500),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Specializations table (master data - shared across tenants)
CREATE TABLE specializations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(100) NOT NULL UNIQUE, -- e.g., "CARDIOLOGY"
    name VARCHAR(255) NOT NULL, -- e.g., "Cardiology"
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Doctor Specializations join table
CREATE TABLE doctor_specializations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    specialization_id UUID NOT NULL REFERENCES specializations(id) ON DELETE CASCADE,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    years_of_experience INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_doctor_specialization UNIQUE (doctor_id, specialization_id)
);

-- Qualifications table
CREATE TABLE qualifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL, -- e.g., "MBBS", "MD"
    institution VARCHAR(255),
    year INTEGER,
    certificate_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Awards table
CREATE TABLE awards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    organization VARCHAR(255),
    year INTEGER,
    description TEXT,
    certificate_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for doctors table
CREATE INDEX idx_doctors_user_id ON doctors(user_id);
CREATE INDEX idx_doctors_tenant_id ON doctors(tenant_id);
CREATE INDEX idx_doctors_domain_id ON doctors(domain_id);
CREATE INDEX idx_doctors_domain_tenant ON doctors(domain_id, tenant_id);
CREATE INDEX idx_doctors_verification_status ON doctors(verification_status);
CREATE INDEX idx_doctors_deleted ON doctors(deleted);

-- Indexes for specializations table
CREATE INDEX idx_specializations_code ON specializations(code);
CREATE INDEX idx_specializations_name ON specializations(name);

-- Indexes for doctor_specializations table
CREATE INDEX idx_doctor_spec_doctor ON doctor_specializations(doctor_id);
CREATE INDEX idx_doctor_spec_specialization ON doctor_specializations(specialization_id);

-- Indexes for qualifications table
CREATE INDEX idx_qualifications_doctor ON qualifications(doctor_id);
CREATE INDEX idx_qualifications_tenant ON qualifications(tenant_id);
CREATE INDEX idx_qualifications_domain ON qualifications(domain_id);

-- Indexes for awards table
CREATE INDEX idx_awards_doctor ON awards(doctor_id);
CREATE INDEX idx_awards_tenant ON awards(tenant_id);
CREATE INDEX idx_awards_domain ON awards(domain_id);

-- Comments
COMMENT ON TABLE doctors IS 'Doctor profiles with verification status and basic information';
COMMENT ON TABLE specializations IS 'Master data for medical specializations (shared across tenants)';
COMMENT ON TABLE doctor_specializations IS 'Many-to-many relationship between doctors and specializations';
COMMENT ON TABLE qualifications IS 'Doctor qualifications (degrees, certifications)';
COMMENT ON TABLE awards IS 'Doctor awards and recognitions';

