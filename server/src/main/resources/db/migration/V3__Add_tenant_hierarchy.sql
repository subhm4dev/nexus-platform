-- Add tenant hierarchy support
-- Enables parent-child relationships for multi-branch management
-- Hierarchy: APP → SELLER → BRANCH

-- Create tenant_type enum
CREATE TYPE tenant_type AS ENUM ('APP', 'BRANCH', 'SELLER');

-- Add parent_tenant_id column for branch support
ALTER TABLE tenants ADD COLUMN parent_tenant_id UUID REFERENCES tenants(id) ON DELETE CASCADE;

-- Add type column to tenants table
ALTER TABLE tenants ADD COLUMN type tenant_type DEFAULT 'APP';

-- Create indexes for hierarchy queries
CREATE INDEX idx_tenants_parent ON tenants(parent_tenant_id);
CREATE INDEX idx_tenants_type ON tenants(type);
CREATE INDEX idx_tenants_type_domain ON tenants(type, domain_id);

-- Comments
COMMENT ON COLUMN tenants.parent_tenant_id IS 'Parent tenant ID for child tenants. NULL for APP tenants. SELLER tenants have parent = APP. BRANCH tenants have parent = SELLER.';
COMMENT ON COLUMN tenants.type IS 'Tenant type: APP (application-level like Namaste Fab, Kalakosh - top-level), SELLER (seller tenant with parent = APP), BRANCH (branch tenant with parent = SELLER)';

