-- ============================================================================
-- Kalakosh - Master Seed Data Script
-- ============================================================================
-- This script runs all seed scripts in the correct order:
-- 1. IAM (tenants, users, roles)
-- 2. Catalog (categories, products)
-- 3. Inventory (locations, stock)
-- ============================================================================
-- Usage:
--   psql -d ecom_iam -f seed-iam.sql
--   psql -d ecom_catalog -f seed-catalog.sql
--   psql -d ecom_inventory -f seed-inventory.sql
-- ============================================================================

-- This is a master script that documents the order of execution.
-- In practice, you would run each script separately against its respective database.

-- Step 1: Run seed-iam.sql against ecom_iam database
-- \i seed-iam.sql

-- Step 2: Run seed-catalog.sql against ecom_catalog database
-- \i seed-catalog.sql

-- Step 3: Run seed-inventory.sql against ecom_inventory database
-- \i seed-inventory.sql

-- ============================================================================
-- Verification Queries
-- ============================================================================
-- After running all scripts, you can verify the data with these queries:

-- Verify tenants (run in ecom_iam):
-- SELECT id, name, status FROM tenants;

-- Verify users (run in ecom_iam):
-- SELECT id, email, tenant_id FROM user_accounts;

-- Verify categories (run in ecom_catalog):
-- SELECT id, name, parent_id FROM categories ORDER BY name;

-- Verify products (run in ecom_catalog):
-- SELECT id, name, sku, price, category_id FROM products ORDER BY name;

-- Verify stock (run in ecom_inventory):
-- SELECT sku, qty_on_hand, location_id FROM stock ORDER BY sku;

