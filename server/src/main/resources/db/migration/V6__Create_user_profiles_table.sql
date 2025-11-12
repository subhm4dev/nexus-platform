-- User Profiles Migration
-- Creates the user_profiles table for storing user profile information
-- Consolidated migration: Includes domain_id from the start with composite unique constraint

CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, -- References user_accounts.id in Identity service (no FK constraint - different service)
    domain_id UUID NOT NULL, -- References domains.id - the business domain this profile belongs to. A user can have multiple profiles, one per domain.
    full_name VARCHAR(255),
    phone VARCHAR(20), -- E.164 format (e.g., +919876543210)
    avatar_url VARCHAR(2048), -- URL to avatar image (stored in object storage)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- User can have one profile per domain
    CONSTRAINT user_profiles_user_domain_unique UNIQUE (user_id, domain_id)
);

-- Index for faster lookups by user_id
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);

-- Domain-related indexes
CREATE INDEX idx_user_profiles_domain ON user_profiles(domain_id);
CREATE INDEX idx_user_profiles_user_domain ON user_profiles(user_id, domain_id);

-- Comment for documentation
COMMENT ON TABLE user_profiles IS 'Stores user profile information (name, phone, avatar) separate from authentication credentials';
COMMENT ON COLUMN user_profiles.user_id IS 'References user_accounts.id in Identity service (no FK - different service/database)';
COMMENT ON COLUMN user_profiles.domain_id IS 'References domains.id - the business domain this profile belongs to. A user can have multiple profiles, one per domain.';
COMMENT ON COLUMN user_profiles.full_name IS 'Full name of the user (e.g., "John Doe")';
COMMENT ON COLUMN user_profiles.phone IS 'Contact phone number in E.164 format (may differ from auth phone)';
COMMENT ON COLUMN user_profiles.avatar_url IS 'URL to user avatar/profile picture (stored in object storage like S3)';
