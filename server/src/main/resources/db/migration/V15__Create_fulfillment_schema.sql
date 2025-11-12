-- Fulfillment Service Schema
-- Consolidated migration: Creates all fulfillment-related tables with all columns from all migrations
-- This consolidates V1-V19 into a single migration

-- Create fulfillments table with all columns
CREATE TABLE fulfillments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    assigned_driver_id UUID,
    pickup_location VARCHAR(500),
    delivery_address_id UUID NOT NULL,
    estimated_delivery TIMESTAMP,
    actual_delivery TIMESTAMP,
    priority VARCHAR(50) DEFAULT 'NORMAL', -- URGENT, HIGH, NORMAL, LOW
    exception_reason VARCHAR(200), -- Reason for exception status
    delivery_instructions TEXT, -- Special delivery instructions
    scheduled_delivery_date TIMESTAMP, -- Scheduled delivery date/time
    delivery_time_window VARCHAR(100), -- Preferred delivery time window
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for fulfillments table
CREATE INDEX idx_fulfillment_order_id ON fulfillments(order_id);
CREATE INDEX idx_fulfillment_tenant_id ON fulfillments(tenant_id);
CREATE INDEX idx_fulfillment_status ON fulfillments(status);
CREATE INDEX idx_fulfillment_driver_id ON fulfillments(assigned_driver_id);
CREATE INDEX idx_fulfillment_created_at ON fulfillments(created_at);

-- Create delivery_providers table (must be created before deliveries)
CREATE TABLE delivery_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    provider_code VARCHAR(50) NOT NULL,  -- BLUEDART, DELHIVERY, SHIPROCKET, DUNZO, RAPIDO, OWN_FLEET
    provider_name VARCHAR(200) NOT NULL,
    provider_type VARCHAR(50) NOT NULL,  -- INTERCITY, INTRACITY, OWN_FLEET
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    api_key VARCHAR(500),
    api_secret VARCHAR(500),
    webhook_secret VARCHAR(500),
    base_url VARCHAR(500),
    config JSONB,  -- Provider-specific configuration
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(tenant_id, provider_code)
);

-- Create indexes for delivery_providers table
CREATE INDEX idx_delivery_provider_tenant_id ON delivery_providers(tenant_id);
CREATE INDEX idx_delivery_provider_code ON delivery_providers(provider_code);
CREATE INDEX idx_delivery_provider_active ON delivery_providers(is_active);

-- Create drivers table
CREATE TABLE drivers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(255),
    vehicle_type VARCHAR(50),
    vehicle_number VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    current_latitude DECIMAL(10, 8), -- Current driver latitude
    current_longitude DECIMAL(11, 8), -- Current driver longitude
    last_location_update TIMESTAMP, -- Last location update timestamp
    earnings DECIMAL(10, 2) DEFAULT 0.00, -- Total earnings for driver
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for drivers table
CREATE INDEX idx_driver_tenant_id ON drivers(tenant_id);
CREATE INDEX idx_driver_status ON drivers(status);
CREATE INDEX idx_driver_phone ON drivers(phone);

-- Create deliveries table with all columns
CREATE TABLE deliveries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fulfillment_id UUID NOT NULL,
    delivery_type VARCHAR(50) NOT NULL DEFAULT 'OWN_FLEET',  -- OWN_FLEET, THIRD_PARTY
    driver_id UUID,  -- NULL for third-party providers
    provider_id UUID,  -- NULL for own fleet
    provider_tracking_id VARCHAR(200),  -- Provider's tracking ID
    tenant_id UUID NOT NULL,
    current_location VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    status VARCHAR(50) NOT NULL DEFAULT 'ASSIGNED',
    tracking_number VARCHAR(100) UNIQUE,
    provider_status VARCHAR(100),  -- Provider's status (e.g., "In Transit", "Out for Delivery")
    attempt_count INTEGER DEFAULT 0, -- Number of delivery attempts
    last_attempt_at TIMESTAMP, -- Timestamp of last delivery attempt
    next_attempt_at TIMESTAMP, -- Scheduled time for next attempt
    failure_reason VARCHAR(200), -- Reason for delivery failure
    cod_amount DECIMAL(10, 2), -- Cash on delivery amount
    cod_collected BOOLEAN DEFAULT FALSE, -- Whether COD was collected
    estimated_arrival TIMESTAMP, -- Estimated time of arrival
    delivery_address_latitude DECIMAL(10, 8),
    delivery_address_longitude DECIMAL(11, 8),
    proximity_radius_meters INTEGER DEFAULT 50,
    requires_dual_confirmation BOOLEAN DEFAULT TRUE,
    confirmation_timeout_minutes INTEGER DEFAULT 5,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT fk_delivery_fulfillment FOREIGN KEY (fulfillment_id) REFERENCES fulfillments(id) ON DELETE CASCADE,
    CONSTRAINT fk_delivery_provider FOREIGN KEY (provider_id) REFERENCES delivery_providers(id) ON DELETE SET NULL
);

-- Create indexes for deliveries table
CREATE INDEX idx_delivery_fulfillment_id ON deliveries(fulfillment_id);
CREATE INDEX idx_delivery_driver_id ON deliveries(driver_id);
CREATE INDEX idx_delivery_provider_id ON deliveries(provider_id);
CREATE INDEX idx_delivery_tenant_id ON deliveries(tenant_id);
CREATE INDEX idx_delivery_status ON deliveries(status);
CREATE INDEX idx_delivery_tracking_number ON deliveries(tracking_number);
CREATE INDEX idx_delivery_provider_tracking_id ON deliveries(provider_tracking_id);

-- Create tracking_history table
CREATE TABLE tracking_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    location_description VARCHAR(500),
    status VARCHAR(50),
    updated_by UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tracking_history_delivery FOREIGN KEY (delivery_id) REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Create indexes for tracking_history table
CREATE INDEX idx_tracking_history_delivery_id ON tracking_history(delivery_id);
CREATE INDEX idx_tracking_history_created_at ON tracking_history(created_at);

-- Create delivery_confirmations table
CREATE TABLE delivery_confirmations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    
    -- Agent confirmation
    agent_confirmed BOOLEAN DEFAULT FALSE,
    agent_confirmed_at TIMESTAMP,
    agent_latitude DECIMAL(10, 8),
    agent_longitude DECIMAL(11, 8),
    agent_location_accuracy DECIMAL(5, 2), -- in meters
    agent_user_id UUID, -- driver/agent user ID
    
    -- Customer confirmation
    customer_confirmed BOOLEAN DEFAULT FALSE,
    customer_confirmed_at TIMESTAMP,
    customer_latitude DECIMAL(10, 8),
    customer_longitude DECIMAL(11, 8),
    customer_location_accuracy DECIMAL(5, 2), -- in meters
    customer_user_id UUID, -- customer user ID
    
    -- Proximity check (flexible location - parties can meet anywhere)
    proximity_verified BOOLEAN DEFAULT FALSE,
    distance_between_parties DECIMAL(8, 2), -- in meters (key check - parties must be close)
    distance_to_delivery_address DECIMAL(8, 2), -- in meters (for records)
    proximity_verified_at TIMESTAMP,
    
    -- Actual delivery location (where they actually met)
    actual_delivery_latitude DECIMAL(10, 8),
    actual_delivery_longitude DECIMAL(11, 8),
    actual_delivery_address TEXT, -- Optional description
    delivery_location_type VARCHAR(50) DEFAULT 'SCHEDULED_ADDRESS',
    -- SCHEDULED_ADDRESS, ALTERNATE_LOCATION, CUSTOMER_LOCATION
    
    -- Not available tracking
    agent_marked_unavailable BOOLEAN DEFAULT FALSE,
    agent_unavailable_at TIMESTAMP,
    agent_unavailable_reason VARCHAR(200),
    customer_marked_unavailable BOOLEAN DEFAULT FALSE,
    customer_unavailable_at TIMESTAMP,
    customer_unavailable_reason VARCHAR(200),
    
    -- Reschedule tracking
    reschedule_count INTEGER DEFAULT 0,
    last_reschedule_at TIMESTAMP,
    next_attempt_at TIMESTAMP,
    auto_return_initiated BOOLEAN DEFAULT FALSE,
    
    -- Age verification (for restricted items like alcohol)
    requires_age_verification BOOLEAN DEFAULT FALSE,
    minimum_age_required INTEGER, -- 18 or 21
    age_verified BOOLEAN DEFAULT FALSE,
    age_verification_method VARCHAR(50),
    -- PHOTO_VERIFICATION, AADHAAR_FACE_RD, ID_VERIFICATION, VIDEO_KYC
    age_verification_status VARCHAR(50),
    -- PENDING, VERIFIED, FAILED, REJECTED
    age_verification_at TIMESTAMP,
    
    -- Alternate recipient fields
    alternate_recipient_id UUID,
    confirmed_by_alternate BOOLEAN DEFAULT FALSE,
    alternate_recipient_name VARCHAR(200),
    alternate_recipient_phone VARCHAR(20),
    
    -- Status
    confirmation_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, AGENT_CONFIRMED, CUSTOMER_CONFIRMED, BOTH_CONFIRMED, 
    -- AGENT_UNAVAILABLE, CUSTOMER_UNAVAILABLE, BOTH_UNAVAILABLE, RETURNED, CONFLICT
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_delivery_confirmation_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Create indexes for delivery_confirmations
CREATE INDEX idx_delivery_confirmation_delivery_id ON delivery_confirmations(delivery_id);
CREATE INDEX idx_delivery_confirmation_status ON delivery_confirmations(confirmation_status);
CREATE INDEX idx_delivery_confirmation_next_attempt ON delivery_confirmations(next_attempt_at);
CREATE INDEX idx_delivery_confirmation_tenant_id ON delivery_confirmations(tenant_id);
CREATE INDEX idx_delivery_confirmation_alternate ON delivery_confirmations(alternate_recipient_id);

-- Create alternate_recipients table
CREATE TABLE alternate_recipients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL,
    delivery_confirmation_id UUID,
    tenant_id UUID NOT NULL,
    customer_user_id UUID NOT NULL, -- Original customer who shared the link
    
    -- Alternate recipient details
    alternate_user_id UUID, -- User ID if they have account
    alternate_phone_number VARCHAR(20), -- Phone number (can be used without account)
    alternate_name VARCHAR(200), -- Name of alternate recipient
    alternate_email VARCHAR(200), -- Email (optional)
    
    -- Sharing details
    share_token VARCHAR(100) UNIQUE NOT NULL, -- Unique token for sharing link
    share_link VARCHAR(500), -- Full shareable link
    shared_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shared_by_user_id UUID NOT NULL, -- User who shared (customer or admin)
    shared_via VARCHAR(50) DEFAULT 'SMS', -- SMS, EMAIL, WHATSAPP, LINK
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', 
    -- PENDING, ACTIVE, CONFIRMED, EXPIRED, REVOKED
    confirmed_at TIMESTAMP,
    confirmed_by_user_id UUID, -- Which alternate user confirmed
    
    -- Confirmation details (when alternate user confirms)
    confirmed_latitude DECIMAL(10, 8),
    confirmed_longitude DECIMAL(11, 8),
    confirmed_location_accuracy DECIMAL(5, 2),
    proximity_verified BOOLEAN DEFAULT FALSE,
    distance_to_agent DECIMAL(8, 2), -- Distance to agent when confirmed
    
    -- Expiry
    expires_at TIMESTAMP, -- Link expiry (default 24 hours)
    revoked_at TIMESTAMP,
    revoked_by_user_id UUID,
    revoke_reason VARCHAR(200),
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_alternate_recipient_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE,
    CONSTRAINT fk_alternate_recipient_confirmation FOREIGN KEY (delivery_confirmation_id) 
        REFERENCES delivery_confirmations(id) ON DELETE SET NULL
);

-- Create indexes for alternate_recipients
CREATE INDEX idx_alternate_recipient_delivery_id ON alternate_recipients(delivery_id);
CREATE INDEX idx_alternate_recipient_confirmation_id ON alternate_recipients(delivery_confirmation_id);
CREATE INDEX idx_alternate_recipient_token ON alternate_recipients(share_token);
CREATE INDEX idx_alternate_recipient_phone ON alternate_recipients(alternate_phone_number);
CREATE INDEX idx_alternate_recipient_user ON alternate_recipients(alternate_user_id);
CREATE INDEX idx_alternate_recipient_status ON alternate_recipients(status);
CREATE INDEX idx_alternate_recipient_customer ON alternate_recipients(customer_user_id);
CREATE INDEX idx_alternate_recipient_expires ON alternate_recipients(expires_at);

-- Create age_verifications table
CREATE TABLE age_verifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_confirmation_id UUID NOT NULL REFERENCES delivery_confirmations(id) ON DELETE CASCADE,
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    customer_user_id UUID NOT NULL,
    
    -- Age requirement
    minimum_age_required INTEGER NOT NULL, -- 18 or 21
    customer_date_of_birth DATE, -- Extracted from verification
    
    -- Verification method
    verification_method VARCHAR(50) NOT NULL,
    -- PHOTO_VERIFICATION, AADHAAR_FACE_RD, ID_VERIFICATION, VIDEO_KYC
    
    -- Photo verification
    customer_photo_url VARCHAR(500),
    id_photo_url VARCHAR(500),
    id_type VARCHAR(50), -- AADHAAR, PAN, DRIVING_LICENSE, PASSPORT
    id_number VARCHAR(100),
    
    -- Aadhaar Face RD verification
    aadhaar_number VARCHAR(12),
    aadhaar_reference_id VARCHAR(100), -- UIDAI reference ID
    aadhaar_transaction_id VARCHAR(100), -- UIDAI transaction ID
    aadhaar_otp_reference_id VARCHAR(100), -- OTP reference ID
    biometric_match_score DECIMAL(5, 2), -- Face match confidence (0-100)
    aadhaar_verified BOOLEAN DEFAULT FALSE,
    aadhaar_demographic_data JSONB, -- Store DOB, name, etc. from UIDAI
    
    -- Verification result
    verification_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, VERIFICATION_IN_PROGRESS, VERIFIED, FAILED, REJECTED, MANUAL_REVIEW
    age_verified BOOLEAN DEFAULT FALSE,
    verified_age INTEGER, -- Calculated age
    verification_confidence DECIMAL(5, 2), -- Overall confidence score (0-100)
    
    -- Alternate recipient fields
    alternate_recipient_id UUID REFERENCES alternate_recipients(id) ON DELETE SET NULL,
    verified_by_alternate BOOLEAN DEFAULT FALSE,
    verified_user_id UUID, -- The actual user who was verified (customer OR alternate)
    verified_user_phone VARCHAR(20), -- Phone of verified user
    verified_user_name VARCHAR(200), -- Name of verified user
    
    -- Metadata
    verified_by_system BOOLEAN DEFAULT TRUE,
    verified_by_admin UUID, -- Admin user ID if manual verification
    verification_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP,
    
    CONSTRAINT fk_age_verification_confirmation FOREIGN KEY (delivery_confirmation_id) 
        REFERENCES delivery_confirmations(id) ON DELETE CASCADE,
    CONSTRAINT fk_age_verification_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE
);

-- Create indexes for age_verifications
CREATE INDEX idx_age_verification_delivery_id ON age_verifications(delivery_id);
CREATE INDEX idx_age_verification_confirmation_id ON age_verifications(delivery_confirmation_id);
CREATE INDEX idx_age_verification_status ON age_verifications(verification_status);
CREATE INDEX idx_age_verification_customer ON age_verifications(customer_user_id);
CREATE INDEX idx_age_verification_tenant ON age_verifications(tenant_id);
CREATE INDEX idx_age_verification_alternate ON age_verifications(alternate_recipient_id);
CREATE INDEX idx_age_verification_verified_user ON age_verifications(verified_user_id);

-- Create delivery_attempts table
CREATE TABLE delivery_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    fulfillment_id UUID REFERENCES fulfillments(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    
    -- Attempt details
    attempt_number INTEGER NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attempted_by_user_id UUID, -- Driver/agent user ID
    attempt_status VARCHAR(50) NOT NULL,
    -- SUCCESSFUL, FAILED, CANCELLED
    
    -- Failure details
    failure_reason VARCHAR(200),
    failure_code VARCHAR(50),
    -- CUSTOMER_NOT_AVAILABLE, WRONG_ADDRESS, DAMAGED_GOODS, REFUSED, etc.
    
    -- Location
    attempt_latitude DECIMAL(10, 8),
    attempt_longitude DECIMAL(11, 8),
    attempt_location_description VARCHAR(500),
    
    -- Photos/Evidence
    photo_urls TEXT[], -- Array of photo URLs
    notes TEXT,
    
    -- Next attempt
    next_attempt_at TIMESTAMP,
    next_attempt_scheduled BOOLEAN DEFAULT FALSE,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for delivery_attempts
CREATE INDEX idx_delivery_attempt_delivery_id ON delivery_attempts(delivery_id);
CREATE INDEX idx_delivery_attempt_fulfillment_id ON delivery_attempts(fulfillment_id);
CREATE INDEX idx_delivery_attempt_status ON delivery_attempts(attempt_status);
CREATE INDEX idx_delivery_attempt_tenant ON delivery_attempts(tenant_id);
CREATE INDEX idx_delivery_attempt_next_attempt ON delivery_attempts(next_attempt_at);

-- Create delivery_attempt_photos table for @ElementCollection
CREATE TABLE delivery_attempt_photos (
    attempt_id UUID NOT NULL REFERENCES delivery_attempts(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (attempt_id, photo_url)
);

CREATE INDEX idx_attempt_photos_attempt_id ON delivery_attempt_photos(attempt_id);

-- Create proof_of_delivery table
CREATE TABLE proof_of_delivery (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    delivery_id UUID NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    fulfillment_id UUID REFERENCES fulfillments(id) ON DELETE CASCADE,
    tenant_id UUID NOT NULL,
    
    -- Delivery confirmation reference
    delivery_confirmation_id UUID REFERENCES delivery_confirmations(id) ON DELETE SET NULL,
    
    -- POD type
    pod_type VARCHAR(50) NOT NULL DEFAULT 'PHOTO',
    -- PHOTO, SIGNATURE, OTP, VIDEO, COMBINATION
    
    -- Photo POD
    photo_urls TEXT[], -- Array of photo URLs
    photo_taken_at TIMESTAMP,
    photo_taken_by_user_id UUID, -- Driver/agent user ID
    
    -- Signature POD
    signature_url VARCHAR(500), -- URL to signature image
    signature_data TEXT, -- Base64 signature data
    signature_taken_at TIMESTAMP,
    signature_taken_by_user_id UUID,
    recipient_name VARCHAR(200), -- Name of person who signed
    
    -- OTP POD
    otp_verified BOOLEAN DEFAULT FALSE,
    otp_code VARCHAR(10),
    otp_verified_at TIMESTAMP,
    otp_phone_number VARCHAR(20),
    
    -- Video POD (optional)
    video_url VARCHAR(500),
    video_taken_at TIMESTAMP,
    
    -- Location
    pod_latitude DECIMAL(10, 8),
    pod_longitude DECIMAL(11, 8),
    pod_location_description VARCHAR(500),
    
    -- Recipient details
    received_by_name VARCHAR(200),
    received_by_phone VARCHAR(20),
    received_by_relation VARCHAR(50), -- SELF, FAMILY_MEMBER, NEIGHBOR, SECURITY, etc.
    is_alternate_recipient BOOLEAN DEFAULT FALSE,
    alternate_recipient_id UUID REFERENCES alternate_recipients(id) ON DELETE SET NULL,
    
    -- Status
    pod_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    -- PENDING, COMPLETED, REJECTED, MANUAL_REVIEW
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    verified_at TIMESTAMP,
    verified_by_user_id UUID, -- Admin who verified (if manual review)
    
    CONSTRAINT fk_pod_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE CASCADE,
    CONSTRAINT fk_pod_confirmation FOREIGN KEY (delivery_confirmation_id) 
        REFERENCES delivery_confirmations(id) ON DELETE SET NULL
);

-- Create indexes for proof_of_delivery
CREATE INDEX idx_pod_delivery_id ON proof_of_delivery(delivery_id);
CREATE INDEX idx_pod_fulfillment_id ON proof_of_delivery(fulfillment_id);
CREATE INDEX idx_pod_confirmation_id ON proof_of_delivery(delivery_confirmation_id);
CREATE INDEX idx_pod_status ON proof_of_delivery(pod_status);
CREATE INDEX idx_pod_tenant ON proof_of_delivery(tenant_id);

-- Create pod_photos table for @ElementCollection
CREATE TABLE pod_photos (
    pod_id UUID NOT NULL REFERENCES proof_of_delivery(id) ON DELETE CASCADE,
    photo_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (pod_id, photo_url)
);

CREATE INDEX idx_pod_photos_pod_id ON pod_photos(pod_id);

-- Create delivery_preferences table
CREATE TABLE delivery_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fulfillment_id UUID NOT NULL REFERENCES fulfillments(id) ON DELETE CASCADE,
    delivery_id UUID REFERENCES deliveries(id) ON DELETE SET NULL,
    tenant_id UUID NOT NULL,
    customer_user_id UUID NOT NULL,
    
    -- Scheduling
    scheduled_delivery_date DATE,
    scheduled_delivery_time_start TIME,
    scheduled_delivery_time_end TIME,
    delivery_time_window VARCHAR(100), -- "MORNING", "AFTERNOON", "EVENING", "9AM-12PM", etc.
    
    -- Delivery instructions
    delivery_instructions TEXT,
    special_handling_notes TEXT,
    leave_at_door BOOLEAN DEFAULT FALSE,
    hand_to_customer BOOLEAN DEFAULT TRUE,
    require_signature BOOLEAN DEFAULT FALSE,
    
    -- Contact preferences
    preferred_contact_method VARCHAR(50), -- PHONE, SMS, EMAIL, APP
    preferred_contact_time VARCHAR(100),
    do_not_disturb BOOLEAN DEFAULT FALSE,
    
    -- Location preferences
    preferred_delivery_location VARCHAR(200), -- "Front door", "Back gate", etc.
    gate_code VARCHAR(50),
    building_name VARCHAR(200),
    floor_number VARCHAR(20),
    apartment_number VARCHAR(50),
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    applied_at TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_preference_fulfillment FOREIGN KEY (fulfillment_id) 
        REFERENCES fulfillments(id) ON DELETE CASCADE,
    CONSTRAINT fk_preference_delivery FOREIGN KEY (delivery_id) 
        REFERENCES deliveries(id) ON DELETE SET NULL
);

-- Create indexes for delivery_preferences
CREATE INDEX idx_preference_fulfillment_id ON delivery_preferences(fulfillment_id);
CREATE INDEX idx_preference_delivery_id ON delivery_preferences(delivery_id);
CREATE INDEX idx_preference_customer ON delivery_preferences(customer_user_id);
CREATE INDEX idx_preference_tenant ON delivery_preferences(tenant_id);
CREATE INDEX idx_preference_scheduled_date ON delivery_preferences(scheduled_delivery_date);

