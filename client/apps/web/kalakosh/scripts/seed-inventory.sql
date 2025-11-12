-- ============================================================================
-- Kalakosh - Inventory Database Seed Script
-- ============================================================================
-- This script creates:
-- 1. A main warehouse location
-- 2. Stock records for all products
-- ============================================================================
-- Database: ecom_inventory
-- ============================================================================

-- ============================================================================
-- 1. CREATE MAIN WAREHOUSE LOCATION
-- ============================================================================
-- Using a fixed UUID so we can reference it in stock records
-- ============================================================================

INSERT INTO locations (id, name, type, address, tenant_id, active, created_at, updated_at)
VALUES (
    '11111111-1111-1111-1111-111111111111'::uuid,
    'Kalakosh Main Warehouse',
    'WAREHOUSE',
    'Raghurajpur, Puri, Odisha 752001',
    '00000000-0000-0000-0000-000000000000'::uuid,
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

-- ============================================================================
-- 2. CREATE STOCK RECORDS FOR ALL PRODUCTS
-- ============================================================================
-- Stock records for each SKU at the main warehouse
-- Initial stock: 10 units per product (artworks are unique/limited)
-- ============================================================================

-- Radha Krishna Eternal Dance
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-MYTH-001',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Dasavatara - Ten Avatars
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-MYTH-002',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Jagannath Rath Yatra
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-FEST-001',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Tree of Life
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-NAT-001',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Ganesha Blessing
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-DEITY-001',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Peacock Dance
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-NAT-002',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Krishna Leela
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-MYTH-003',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

-- Village Life
INSERT INTO stock (id, sku, location_id, qty_on_hand, reserved_qty, tenant_id, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'KAL-LIFE-001',
    '11111111-1111-1111-1111-111111111111'::uuid,
    10,
    0,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (sku, location_id, tenant_id) DO UPDATE
SET 
    qty_on_hand = EXCLUDED.qty_on_hand,
    updated_at = CURRENT_TIMESTAMP;

