-- Patient Service Schema Migration
-- Creates tables for patients, medical history, allergies, medications, insurance, and emergency contacts

-- Patients table
CREATE TABLE patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, -- References user_accounts.id in Identity service
    tenant_id UUID NOT NULL, -- Multi-tenant isolation (BRANCH tenant)
    domain_id UUID NOT NULL, -- References domains.id (healthcare domain)
    date_of_birth DATE,
    gender VARCHAR(20), -- MALE, FEMALE, OTHER
    blood_group VARCHAR(10), -- A+, B+, O+, AB+, A-, B-, O-, AB-
    height_cm INTEGER,
    weight_kg DECIMAL(5, 2),
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Patient Medical History table
CREATE TABLE patient_medical_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    condition_name VARCHAR(255) NOT NULL,
    diagnosis_date DATE,
    status VARCHAR(50), -- ACTIVE, RESOLVED, CHRONIC
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Patient Allergies table
CREATE TABLE patient_allergies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    allergen_name VARCHAR(255) NOT NULL,
    severity VARCHAR(50), -- MILD, MODERATE, SEVERE
    reaction_description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Patient Medications table
CREATE TABLE patient_medications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date DATE,
    end_date DATE,
    prescribed_by UUID, -- Doctor ID
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Patient Insurance table
CREATE TABLE patient_insurance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    insurance_provider VARCHAR(255) NOT NULL,
    policy_number VARCHAR(100),
    group_number VARCHAR(100),
    expiry_date DATE,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Patient Emergency Contacts table
CREATE TABLE patient_emergency_contacts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(100), -- SPOUSE, PARENT, SIBLING, FRIEND, etc.
    phone_number VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    address TEXT,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for patients table
CREATE INDEX idx_patients_user_id ON patients(user_id);
CREATE INDEX idx_patients_tenant_id ON patients(tenant_id);
CREATE INDEX idx_patients_domain_id ON patients(domain_id);
CREATE INDEX idx_patients_domain_tenant ON patients(domain_id, tenant_id);
CREATE INDEX idx_patients_deleted ON patients(deleted);

-- Indexes for patient_medical_history table
CREATE INDEX idx_medical_history_patient ON patient_medical_history(patient_id);
CREATE INDEX idx_medical_history_tenant ON patient_medical_history(tenant_id);
CREATE INDEX idx_medical_history_domain ON patient_medical_history(domain_id);

-- Indexes for patient_allergies table
CREATE INDEX idx_allergies_patient ON patient_allergies(patient_id);
CREATE INDEX idx_allergies_tenant ON patient_allergies(tenant_id);
CREATE INDEX idx_allergies_domain ON patient_allergies(domain_id);

-- Indexes for patient_medications table
CREATE INDEX idx_medications_patient ON patient_medications(patient_id);
CREATE INDEX idx_medications_tenant ON patient_medications(tenant_id);
CREATE INDEX idx_medications_domain ON patient_medications(domain_id);

-- Indexes for patient_insurance table
CREATE INDEX idx_insurance_patient ON patient_insurance(patient_id);
CREATE INDEX idx_insurance_tenant ON patient_insurance(tenant_id);
CREATE INDEX idx_insurance_domain ON patient_insurance(domain_id);

-- Indexes for patient_emergency_contacts table
CREATE INDEX idx_emergency_contacts_patient ON patient_emergency_contacts(patient_id);
CREATE INDEX idx_emergency_contacts_tenant ON patient_emergency_contacts(tenant_id);
CREATE INDEX idx_emergency_contacts_domain ON patient_emergency_contacts(domain_id);

-- Comments
COMMENT ON TABLE patients IS 'Patient profiles with basic demographic and health information';
COMMENT ON TABLE patient_medical_history IS 'Patient medical history and conditions';
COMMENT ON TABLE patient_allergies IS 'Patient allergies and reactions';
COMMENT ON TABLE patient_medications IS 'Patient current and past medications';
COMMENT ON TABLE patient_insurance IS 'Patient insurance information';
COMMENT ON TABLE patient_emergency_contacts IS 'Patient emergency contact information';

