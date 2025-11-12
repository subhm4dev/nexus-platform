-- ============================================================================
-- Kalakosh - Comprehensive Seed Script for Pattachitra Products
-- ============================================================================
-- This script seeds data across all databases:
-- 1. IAM: New seller user
-- 2. Address Book: Seller address
-- 3. Catalog: Categories and products from mockData.ts
-- 4. Inventory: Warehouse location and stock records
-- ============================================================================
-- 
-- Execution Instructions:
-- 1. IAM Database: psql -d ecom_iam -f seed-pattachitra-complete.sql
-- 2. Address Book Database: psql -d ecom_address_book -f seed-pattachitra-complete.sql
-- 3. Catalog Database: psql -d ecom_catalog -f seed-pattachitra-complete.sql
-- 4. Inventory Database: psql -d ecom_inventory -f seed-pattachitra-complete.sql
-- 
-- OR run each section separately based on the database
-- ============================================================================

-- ============================================================================
-- PART 1: IAM DATABASE (ecom_iam)
-- ============================================================================
-- Creates new seller user for Pattachitra products
-- ============================================================================

-- New Seller User ID (generate new UUID)
-- Using: cccccccc-cccc-cccc-cccc-cccccccccccc
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
    gen_random_uuid(),
    'seller@pattachitra.art',
    '+919876543212',
    '$argon2id$v=19$m=65536,t=5,p=1$dGVzdHNhbHQxMjM0NTY3ODkwYWJjZGVmZ2hpams$testhash1234567890abcdefghijklmnopqrstuvwxyz',
    'dGVzdHNhbHQxMjM0NTY3ODkwYWJjZGVmZ2hpams=',
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, -- Kalakosh tenant ID
    true,
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email, tenant_id) DO NOTHING;

-- Grant SELLER role to new seller user
INSERT INTO role_grants (id, user_id, role, granted_at)
VALUES (
    gen_random_uuid(),
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    'SELLER',
    CURRENT_TIMESTAMP
)
ON CONFLICT (user_id, role) DO NOTHING;

-- ============================================================================
-- PART 2: ADDRESS BOOK DATABASE (ecom_address_book)
-- ============================================================================
-- Creates seller address
-- ============================================================================

INSERT INTO addresses (
    id,
    user_id,
    tenant_id,
    domain_id,
    line1,
    line2,
    city,
    state,
    postcode,
    country,
    label,
    is_default,
    deleted,
    deleted_at,
    created_at,
    updated_at
)
VALUES (
    'cccccccc-1111-1111-1111-111111111111'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, -- Seller user ID
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, -- Kalakosh tenant ID
    '2c0bec00-e641-41ee-aad4-22c6a9697165'::uuid, -- E-commerce domain ID
    'Raghurajpur Heritage Village',
    'Near Jagannath Temple',
    'Puri',
    'Odisha',
    '752001',
    'IN',
    'Studio Address',
    true,
    false,
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT DO NOTHING;

-- ============================================================================
-- PART 3: CATALOG DATABASE (ecom_catalog)
-- ============================================================================
-- Creates categories and products from mockData.ts
-- ============================================================================

-- Create Categories (if not exist)
INSERT INTO categories (id, name, description, parent_id, tenant_id, created_at, updated_at)
VALUES
    -- Mythology (Main Category)
    ('c1111111-1111-1111-1111-111111111111'::uuid, 'Mythology', 'Traditional mythological themes and divine narratives', NULL, '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Festival (Main Category)
    ('c2222222-2222-2222-2222-222222222222'::uuid, 'Festival', 'Festival-themed Pattachitra artworks', NULL, '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Nature (Main Category)
    ('c3333333-3333-3333-3333-333333333333'::uuid, 'Nature', 'Nature and wildlife themed Pattachitra', NULL, '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Life (Main Category)
    ('c4444444-4444-4444-4444-444444444444'::uuid, 'Life', 'Daily life and village scenes', NULL, '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Deity (Main Category)
    ('c5555555-5555-5555-5555-555555555555'::uuid, 'Deity', 'Deity portraits and divine representations', NULL, '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name, tenant_id) DO NOTHING;

-- Create Products from mockData.ts
-- Product 1: Radha Krishna Eternal Dance
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p1111111-1111-1111-1111-111111111111'::uuid,
    'Radha Krishna Eternal Dance',
    'KAL-MYTH-001',
    'A mesmerizing depiction of the divine love between Radha and Krishna, painted using traditional Pattachitra techniques passed down through generations. Artist: Rajesh Mahapatra. Size: 24" × 36". Materials: Natural colors on palm leaf.',
    45000.00,
    'INR',
    'c1111111-1111-1111-1111-111111111111'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, -- New seller ID
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, -- Kalakosh tenant ID
    '["https://images.unsplash.com/photo-1661708733162-287f585730ae?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMEluZGlhbiUyMHBhaW50aW5nJTIwYXJ0fGVufDF8fHx8MTc2MjY2OTU0NHww&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- Product 2: Jagannath Rath Yatra
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p2222222-2222-2222-2222-222222222222'::uuid,
    'Jagannath Rath Yatra',
    'KAL-FEST-001',
    'An intricate portrayal of the famous Rath Yatra festival, showcasing the grandeur and devotion of this ancient tradition. Artist: Sushila Dash. Size: 18" × 24". Materials: Natural pigments on cloth.',
    38000.00,
    'INR',
    'c2222222-2222-2222-2222-222222222222'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    '["https://images.unsplash.com/photo-1762173886363-de541417e48e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjBmb2xrJTIwYXJ0JTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDV8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- Product 3: Dasavatara - Ten Avatars
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p3333333-3333-3333-3333-333333333333'::uuid,
    'Dasavatara - Ten Avatars',
    'KAL-MYTH-002',
    'A magnificent representation of Lord Vishnu''s ten incarnations, each detail meticulously crafted by a master artisan. Artist: Ramesh Pattnaik. Size: 30" × 40". Materials: Natural colors on treated canvas.',
    65000.00,
    'INR',
    'c1111111-1111-1111-1111-111111111111'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    '["https://images.unsplash.com/photo-1759108811633-173f7104a7d8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMGFydCUyMHBhaW50aW5nJTIwZGV0YWlsZWR8ZW58MXx8fHwxNzYyNjY5NTQ1fDA&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- Product 4: Tree of Life
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p4444444-4444-4444-4444-444444444444'::uuid,
    'Tree of Life',
    'KAL-NAT-001',
    'A symbolic representation of life and nature, adorned with traditional motifs and vibrant colors. Artist: Anita Swain. Size: 16" × 20". Materials: Organic dyes on silk.',
    28000.00,
    'INR',
    'c3333333-3333-3333-3333-333333333333'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    '["https://images.unsplash.com/photo-1758169744470-097641049fb7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjB0ZXh0aWxlJTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDZ8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- Product 5: Ganesha Blessing
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p5555555-5555-5555-5555-555555555555'::uuid,
    'Ganesha Blessing',
    'KAL-DEITY-001',
    'Lord Ganesha depicted in traditional Pattachitra style, bringing blessings and prosperity. Artist: Rajesh Mahapatra. Size: 20" × 24". Materials: Natural colors on palm leaf.',
    32000.00,
    'INR',
    'c5555555-5555-5555-5555-555555555555'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    '["https://images.unsplash.com/photo-1661708733162-287f585730ae?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMEluZGlhbiUyMHBhaW50aW5nJTIwYXJ0fGVufDF8fHx8MTc2MjY2OTU0NHww&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- Product 6: Peacock Dance
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p6666666-6666-6666-6666-666666666666'::uuid,
    'Peacock Dance',
    'KAL-NAT-002',
    'A vibrant celebration of the peacock, India''s national bird, in full display. Artist: Sushila Dash. Size: 14" × 18". Materials: Natural pigments on cloth.',
    24000.00,
    'INR',
    'c3333333-3333-3333-3333-333333333333'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    '["https://images.unsplash.com/photo-1762173886363-de541417e48e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjBmb2xrJTIwYXJ0JTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDV8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- Product 7: Krishna Leela
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p7777777-7777-7777-7777-777777777777'::uuid,
    'Krishna Leela',
    'KAL-MYTH-003',
    'The playful childhood stories of Lord Krishna, beautifully captured in traditional art form. Artist: Ramesh Pattnaik. Size: 26" × 34". Materials: Natural colors on treated canvas.',
    52000.00,
    'INR',
    'c1111111-1111-1111-1111-111111111111'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    '["https://images.unsplash.com/photo-1759108811633-173f7104a7d8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMGFydCUyMHBhaW50aW5nJTIwZGV0YWlsZWR8ZW58MXx8fHwxNzYyNjY5NTQ1fDA&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- Product 8: Village Life
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES (
    'p8888888-8888-8888-8888-888888888888'::uuid,
    'Village Life',
    'KAL-LIFE-001',
    'A charming depiction of rural Indian life, celebrating simplicity and tradition. Artist: Anita Swain. Size: 12" × 16". Materials: Organic dyes on silk.',
    22000.00,
    'INR',
    'c4444444-4444-4444-4444-444444444444'::uuid,
    'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    '["https://images.unsplash.com/photo-1758169744470-097641049fb7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjB0ZXh0aWxlJTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDZ8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
    'ACTIVE',
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, tenant_id) DO NOTHING;

-- ============================================================================
-- PART 4: INVENTORY DATABASE (ecom_inventory)
-- ============================================================================
-- Creates warehouse location and stock records
-- ============================================================================

-- Create Warehouse Location
INSERT INTO locations (id, name, type, address, tenant_id, active, created_at, updated_at)
VALUES (
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    'Pattachitra Art Studio Warehouse',
    'WAREHOUSE',
    'Raghurajpur, Puri, Odisha 752001',
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid, -- Kalakosh tenant ID
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (name, tenant_id) DO UPDATE
SET 
    type = EXCLUDED.type,
    address = EXCLUDED.address,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

-- Create Stock Records for All Products
-- Product 1: Radha Krishna Eternal Dance (KAL-MYTH-001)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-MYTH-001',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Product 2: Jagannath Rath Yatra (KAL-FEST-001)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-FEST-001',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Product 3: Dasavatara - Ten Avatars (KAL-MYTH-002)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-MYTH-002',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Product 4: Tree of Life (KAL-NAT-001)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-NAT-001',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Product 5: Ganesha Blessing (KAL-DEITY-001)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-DEITY-001',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Product 6: Peacock Dance (KAL-NAT-002)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-NAT-002',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Product 7: Krishna Leela (KAL-MYTH-003)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-MYTH-003',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Product 8: Village Life (KAL-LIFE-001)
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-LIFE-001',
    'cccccccc-2222-2222-2222-222222222222'::uuid,
    10,
    0,
    '371e4723-6d8c-40d2-934e-dd82a80e6541'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

