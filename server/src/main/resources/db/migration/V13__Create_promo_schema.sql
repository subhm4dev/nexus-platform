-- Promo Service Schema
-- Consolidated migration: Creates promotions and coupons tables

-- Create promotions table
CREATE TABLE promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y
    discount_type VARCHAR(50) NOT NULL, -- PERCENTAGE, FIXED
    discount_value DECIMAL(19, 2) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    eligibility_criteria TEXT, -- JSON string for product IDs, category IDs, etc.
    priority INTEGER DEFAULT 0, -- Higher priority promotions applied first
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for promotions
CREATE INDEX idx_promotions_tenant ON promotions(tenant_id);
CREATE INDEX idx_promotions_active ON promotions(active);
CREATE INDEX idx_promotions_dates ON promotions(start_date, end_date);
CREATE INDEX idx_promotions_priority ON promotions(priority DESC);

-- Add comment for promotions
COMMENT ON TABLE promotions IS 'Stores promotional rules for discounts and pricing';

-- Create coupons table
CREATE TABLE coupons (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    discount_type VARCHAR(50) NOT NULL, -- PERCENTAGE, FIXED
    discount_value DECIMAL(19, 2) NOT NULL,
    usage_limit INTEGER, -- NULL means unlimited
    used_count INTEGER DEFAULT 0,
    expiry_date TIMESTAMP NOT NULL,
    min_order_value DECIMAL(19, 2), -- Minimum order value to apply coupon
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for coupons
CREATE INDEX idx_coupons_tenant ON coupons(tenant_id);
CREATE INDEX idx_coupons_code ON coupons(code);
CREATE INDEX idx_coupons_active ON coupons(active);
CREATE INDEX idx_coupons_expiry ON coupons(expiry_date);

-- Add comment for coupons
COMMENT ON TABLE coupons IS 'Stores coupon codes for discounts';

