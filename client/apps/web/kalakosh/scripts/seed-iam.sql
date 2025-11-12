-- ============================================================================
-- Kalakosh - IAM Database Seed Script
-- ============================================================================
-- This script creates:
-- 1. Admin user
-- 2. Seller user (for artists)
-- 3. Tenants
-- 4. Role grants
-- ============================================================================
-- Database: ecom_iam
-- ============================================================================

-- ============================================================================
-- 1. CREATE TENANTS
-- ============================================================================

-- Ensure the marketplace tenant exists
INSERT INTO tenants (id, name, status, created_at, updated_at)
VALUES (
    '00000000-0000-0000-0000-000000000000'::uuid,
    'Marketplace',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Create Kalakosh seller tenant
INSERT INTO tenants (id, name, status, created_at, updated_at)
VALUES (
    '11111111-1111-1111-1111-111111111111'::uuid,
    'Kalakosh Seller',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 2. CREATE ADMIN USER
-- ============================================================================
-- Password: Admin@123
-- Note: This is a placeholder hash. In production, use the registration API
-- to create users with proper Argon2 hashes.
-- ============================================================================

INSERT INTO user_accounts (
    id,
    email,
    phone,
    password_hash,
    salt,
    tenant_id,
    enabled,
    email_verified,
    phone_verified,
    created_at,
    updated_at
)
VALUES (
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid,
    'admin@kalakosh.art',
    '+919876543210',
    '$argon2id$v=19$m=65536,t=5,p=1$dGVzdHNhbHQxMjM0NTY3ODkwYWJjZGVmZ2hpams$testhash1234567890abcdefghijklmnopqrstuvwxyz',
    'dGVzdHNhbHQxMjM0NTY3ODkwYWJjZGVmZ2hpams=',
    '00000000-0000-0000-0000-000000000000'::uuid,
    true,
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email, tenant_id) DO NOTHING;

-- Grant ADMIN role to admin user
INSERT INTO role_grants (id, user_id, role, granted_at)
VALUES (
    gen_random_uuid(),
    'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid,
    'ADMIN',
    CURRENT_TIMESTAMP
)
ON CONFLICT (user_id, role) DO NOTHING;

-- ============================================================================
-- 3. CREATE SELLER USER (for artists)
-- ============================================================================
-- Password: Seller@123
-- ============================================================================

INSERT INTO user_accounts (
    id,
    email,
    phone,
    password_hash,
    salt,
    tenant_id,
    enabled,
    email_verified,
    phone_verified,
    created_at,
    updated_at
)
VALUES (
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
    'seller@kalakosh.art',
    '+919876543211',
    '$argon2id$v=19$m=65536,t=5,p=1$dGVzdHNhbHQxMjM0NTY3ODkwYWJjZGVmZ2hpams$testhash1234567890abcdefghijklmnopqrstuvwxyz',
    'dGVzdHNhbHQxMjM0NTY3ODkwYWJjZGVmZ2hpams=',
    '11111111-1111-1111-1111-111111111111'::uuid,
    true,
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email, tenant_id) DO NOTHING;

-- Grant SELLER role to seller user
INSERT INTO role_grants (id, user_id, role, granted_at)
VALUES (
    gen_random_uuid(),
    'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
    'SELLER',
    CURRENT_TIMESTAMP
)
ON CONFLICT (user_id, role) DO NOTHING;

