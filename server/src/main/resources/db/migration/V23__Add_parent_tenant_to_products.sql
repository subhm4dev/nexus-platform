-- Add parent_tenant_id to products table
-- Enables seller ownership tracking and tenant hierarchy queries
-- Note: tenants table is in IAM service database, so no foreign key constraint

-- Add parent_tenant_id column (no FK constraint - cross-database reference)
ALTER TABLE products ADD COLUMN parent_tenant_id UUID;

-- Create index for parent_tenant_id queries
CREATE INDEX idx_products_parent_tenant ON products(parent_tenant_id);

-- Note: Cannot update existing products here because user_accounts table is in IAM database
-- Existing products will need parent_tenant_id set via application logic or manual update
-- For new products, parent_tenant_id will be set during product creation

-- Comments
COMMENT ON COLUMN products.parent_tenant_id IS 'Seller tenant ID. Points to SELLER tenant that owns this product. Used for seller-level queries and tenant hierarchy operations. Always set to the SELLER tenant, not APP or BRANCH.';
COMMENT ON COLUMN products.tenant_id IS 'Availability location: BRANCH tenant (branch-specific product) OR SELLER tenant (product available at all seller branches). Used for inventory management and location-based filtering.';
COMMENT ON COLUMN products.seller_id IS 'User ID of the seller who created/owns this product. Used for user-level operations, permissions, and audit trails. Points to user_accounts.id.';

