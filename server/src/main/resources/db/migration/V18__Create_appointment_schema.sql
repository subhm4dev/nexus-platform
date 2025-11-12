-- Appointment Service Schema Migration
-- Creates tables for appointments, availabilities, time-offs, and refund policies

-- Appointments table
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    session_type_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL, -- Calculated: start_time + session_duration
    status VARCHAR(50) NOT NULL, -- SCHEDULED, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW
    payment_status VARCHAR(50), -- PAYMENT_PENDING, PAYMENT_PARTIAL, PAYMENT_COMPLETED, PAYMENT_REFUNDED
    payment_id UUID, -- References payments.id
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    version INTEGER NOT NULL DEFAULT 0, -- For optimistic locking
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    
    CONSTRAINT chk_end_after_start CHECK (end_time > start_time)
);

-- Unique constraint to prevent double-booking (excluding cancelled and deleted)
CREATE UNIQUE INDEX unique_doctor_slot ON appointments(doctor_id, start_time, deleted) 
    WHERE deleted = FALSE AND status != 'CANCELLED';

-- Availabilities table (doctor's regular schedule)
CREATE TABLE availabilities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    day_of_week INTEGER NOT NULL, -- 1=Monday, 7=Sunday
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_availability_time CHECK (end_time > start_time)
);

-- Time Offs table (doctor's time-off periods)
CREATE TABLE time_offs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_timeoff_time CHECK (end_time > start_time)
);

-- Refund Policies table
CREATE TABLE refund_policies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    domain_id UUID NOT NULL,
    policy_type VARCHAR(50) NOT NULL, -- CANCELLATION, NO_SHOW
    refund_percentage DECIMAL(5,2) NOT NULL, -- 100.00 for full, 50.00 for partial
    applicable_hours_before INTEGER, -- Hours before appointment (for cancellation)
    is_automatic BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_tenant_policy_type UNIQUE (tenant_id, domain_id, policy_type)
);

-- Indexes for appointments table
CREATE INDEX idx_appointments_doctor_date ON appointments(doctor_id, start_time) 
    WHERE deleted = FALSE AND status != 'CANCELLED';
CREATE INDEX idx_appointments_patient ON appointments(patient_id) 
    WHERE deleted = FALSE;
CREATE INDEX idx_appointments_status ON appointments(status) 
    WHERE deleted = FALSE;
CREATE INDEX idx_appointments_payment_status ON appointments(payment_status) 
    WHERE deleted = FALSE;
CREATE INDEX idx_appointments_tenant ON appointments(tenant_id, domain_id) 
    WHERE deleted = FALSE;
CREATE INDEX idx_appointments_payment_id ON appointments(payment_id);

-- Indexes for availabilities table
CREATE INDEX idx_availabilities_doctor ON availabilities(doctor_id);
CREATE INDEX idx_availabilities_doctor_day ON availabilities(doctor_id, day_of_week);
CREATE INDEX idx_availabilities_tenant ON availabilities(tenant_id, domain_id);

-- Indexes for time_offs table
CREATE INDEX idx_timeoffs_doctor ON time_offs(doctor_id);
CREATE INDEX idx_timeoffs_doctor_date ON time_offs(doctor_id, start_time, end_time);
CREATE INDEX idx_timeoffs_tenant ON time_offs(tenant_id, domain_id);

-- Indexes for refund_policies table
CREATE INDEX idx_refund_policies_tenant ON refund_policies(tenant_id, domain_id);
CREATE INDEX idx_refund_policies_type ON refund_policies(policy_type, is_active);

-- Comments
COMMENT ON TABLE appointments IS 'Appointment bookings with slot management and payment integration';
COMMENT ON TABLE availabilities IS 'Doctor regular schedule (day of week, time ranges)';
COMMENT ON TABLE time_offs IS 'Doctor time-off periods (holidays, leaves)';
COMMENT ON TABLE refund_policies IS 'Configurable refund policies for cancellations and no-shows';

