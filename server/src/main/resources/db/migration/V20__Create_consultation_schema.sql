-- Consultation Service Schema Migration
-- Creates tables for consultations, prescriptions, and consultation notes

-- Consultations table
CREATE TABLE consultations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    appointment_id UUID NOT NULL, -- References appointments.id
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    consultation_date TIMESTAMP NOT NULL,
    diagnosis TEXT,
    notes TEXT,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Prescriptions table
CREATE TABLE prescriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration_days INTEGER,
    instructions TEXT,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Consultation Notes table
CREATE TABLE consultation_notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    consultation_id UUID NOT NULL REFERENCES consultations(id) ON DELETE CASCADE,
    note_type VARCHAR(50), -- GENERAL, SYMPTOM, EXAMINATION, DIAGNOSIS, TREATMENT
    content TEXT NOT NULL,
    created_by UUID NOT NULL, -- User ID who created the note
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for consultations table
CREATE INDEX idx_consultations_appointment ON consultations(appointment_id);
CREATE INDEX idx_consultations_doctor ON consultations(doctor_id);
CREATE INDEX idx_consultations_patient ON consultations(patient_id);
CREATE INDEX idx_consultations_tenant ON consultations(tenant_id, domain_id);

-- Indexes for prescriptions table
CREATE INDEX idx_prescriptions_consultation ON prescriptions(consultation_id);
CREATE INDEX idx_prescriptions_tenant ON prescriptions(tenant_id, domain_id);

-- Indexes for consultation_notes table
CREATE INDEX idx_consultation_notes_consultation ON consultation_notes(consultation_id);
CREATE INDEX idx_consultation_notes_tenant ON consultation_notes(tenant_id, domain_id);

-- Comments
COMMENT ON TABLE consultations IS 'Consultation records linked to appointments';
COMMENT ON TABLE prescriptions IS 'Prescriptions prescribed during consultations';
COMMENT ON TABLE consultation_notes IS 'Detailed notes for consultations';

