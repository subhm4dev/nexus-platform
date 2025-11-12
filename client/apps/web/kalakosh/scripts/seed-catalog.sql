-- ============================================================================
-- Kalakosh - Catalog Database Seed Script
-- ============================================================================
-- This script creates:
-- 1. Categories for Pattachitra artworks
-- 2. Products from mockData.ts
-- ============================================================================
-- Database: ecom_catalog
-- ============================================================================

-- ============================================================================
-- 1. CREATE CATEGORIES
-- ============================================================================
-- Using marketplace tenant ID for public browsing
-- ============================================================================

-- Main Categories for Pattachitra
INSERT INTO categories (id, name, description, parent_id, tenant_id, created_at, updated_at)
VALUES
    -- Mythology (Main Category)
    ('c1111111-1111-1111-1111-111111111111'::uuid, 'Mythology', 'Traditional mythological themes and divine narratives', NULL, '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Festival (Main Category)
    ('c2222222-2222-2222-2222-222222222222'::uuid, 'Festival', 'Festival-themed Pattachitra artworks', NULL, '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Nature (Main Category)
    ('c3333333-3333-3333-3333-333333333333'::uuid, 'Nature', 'Nature and wildlife themed Pattachitra', NULL, '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Life (Main Category)
    ('c4444444-4444-4444-4444-444444444444'::uuid, 'Life', 'Daily life and village scenes', NULL, '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    
    -- Deity (Main Category)
    ('c5555555-5555-5555-5555-555555555555'::uuid, 'Deity', 'Deity portraits and divine representations', NULL, '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (name, tenant_id) DO NOTHING;

-- ============================================================================
-- 2. CREATE PRODUCTS
-- ============================================================================
-- Products from mockData.ts
-- Seller ID: bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb
-- Tenant ID: 11111111-1111-1111-1111-111111111111 (seller tenant)
-- ============================================================================

-- Mythology Products
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES
    (
        'p1111111-1111-1111-1111-111111111111'::uuid,
        'Radha Krishna Eternal Dance',
        'KAL-MYTH-001',
        'A mesmerizing depiction of the divine love between Radha and Krishna, painted using traditional Pattachitra techniques passed down through generations.',
        45000.00,
        'INR',
        'c1111111-1111-1111-1111-111111111111'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1661708733162-287f585730ae?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMEluZGlhbiUyMHBhaW50aW5nJTIwYXJ0fGVufDF8fHx8MTc2MjY2OTU0NHww&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'p2222222-2222-2222-2222-222222222222'::uuid,
        'Dasavatara - Ten Avatars',
        'KAL-MYTH-002',
        'A magnificent representation of Lord Vishnu''s ten incarnations, each detail meticulously crafted by a master artisan.',
        65000.00,
        'INR',
        'c1111111-1111-1111-1111-111111111111'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1759108811633-173f7104a7d8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMGFydCUyMHBhaW50aW5nJTIwZGV0YWlsZWR8ZW58MXx8fHwxNzYyNjY5NTQ1fDA&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'p7777777-7777-7777-7777-777777777777'::uuid,
        'Krishna Leela',
        'KAL-MYTH-003',
        'The playful childhood stories of Lord Krishna, beautifully captured in traditional art form.',
        52000.00,
        'INR',
        'c1111111-1111-1111-1111-111111111111'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1759108811633-173f7104a7d8?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMGFydCUyMHBhaW50aW5nJTIwZGV0YWlsZWR8ZW58MXx8fHwxNzYyNjY5NTQ1fDA&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- Festival Products
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES
    (
        'p3333333-3333-3333-3333-333333333333'::uuid,
        'Jagannath Rath Yatra',
        'KAL-FEST-001',
        'An intricate portrayal of the famous Rath Yatra festival, showcasing the grandeur and devotion of this ancient tradition.',
        38000.00,
        'INR',
        'c2222222-2222-2222-2222-222222222222'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1762173886363-de541417e48e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjBmb2xrJTIwYXJ0JTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDV8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- Nature Products
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES
    (
        'p4444444-4444-4444-4444-444444444444'::uuid,
        'Tree of Life',
        'KAL-NAT-001',
        'A symbolic representation of life and nature, adorned with traditional motifs and vibrant colors.',
        28000.00,
        'INR',
        'c3333333-3333-3333-3333-333333333333'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1758169744470-097641049fb7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjB0ZXh0aWxlJTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDZ8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    (
        'p6666666-6666-6666-6666-666666666666'::uuid,
        'Peacock Dance',
        'KAL-NAT-002',
        'A vibrant celebration of the peacock, India''s national bird, in full display.',
        24000.00,
        'INR',
        'c3333333-3333-3333-3333-333333333333'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1762173886363-de541417e48e?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjBmb2xrJTIwYXJ0JTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDV8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- Deity Products
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES
    (
        'p5555555-5555-5555-5555-555555555555'::uuid,
        'Ganesha Blessing',
        'KAL-DEITY-001',
        'Lord Ganesha depicted in traditional Pattachitra style, bringing blessings and prosperity.',
        32000.00,
        'INR',
        'c5555555-5555-5555-5555-555555555555'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1661708733162-287f585730ae?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHx0cmFkaXRpb25hbCUyMEluZGlhbiUyMHBhaW50aW5nJTIwYXJ0fGVufDF8fHx8MTc2MjY2OTU0NHww&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- Life Products
INSERT INTO products (id, name, sku, description, price, currency, category_id, seller_id, tenant_id, images, status, deleted, created_at, updated_at)
VALUES
    (
        'p8888888-8888-8888-8888-888888888888'::uuid,
        'Village Life',
        'KAL-LIFE-001',
        'A charming depiction of rural Indian life, celebrating simplicity and tradition.',
        22000.00,
        'INR',
        'c4444444-4444-4444-4444-444444444444'::uuid,
        'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid,
        '11111111-1111-1111-1111-111111111111'::uuid,
        '["https://images.unsplash.com/photo-1758169744470-097641049fb7?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&ixid=M3w3Nzg4Nzd8MHwxfHNlYXJjaHwxfHxJbmRpYW4lMjB0ZXh0aWxlJTIwcGF0dGVybnxlbnwxfHx8fDE3NjI2Njk1NDZ8MA&ixlib=rb-4.1.0&q=80&w=1080"]',
        'ACTIVE',
        false,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    );

-- ============================================================================
-- 3. ADD PRODUCT ATTRIBUTES (Size, Materials, Artist)
-- ============================================================================
-- Note: Product attributes can be stored in product_attributes table if exists
-- or as JSON in a metadata field. For now, we'll add them as notes.
-- ============================================================================

-- Update products with size and materials information
-- Note: This assumes there's a way to store attributes. Adjust based on your schema.

