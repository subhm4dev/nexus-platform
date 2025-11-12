-- Insert default APP tenants for ecommerce domain
-- Namaste Fab (single-vendor app) and Kalakosh (multi-vendor app)

-- Get ecommerce domain ID and insert APP tenants
DO $$
DECLARE
    ecommerce_domain_id UUID;
    namaste_fab_id UUID;
    kalakosh_id UUID;
BEGIN
    -- Get ecommerce domain ID
    SELECT id INTO ecommerce_domain_id FROM domains WHERE code = 'ecommerce';
    
    IF ecommerce_domain_id IS NULL THEN
        RAISE EXCEPTION 'Ecommerce domain not found. Please ensure V2__Insert_default_domains.sql has run.';
    END IF;
    
    -- Insert Namaste Fab (APP tenant)
    INSERT INTO tenants (id, name, status, domain_id, type, created_at, updated_at)
    VALUES (
        gen_random_uuid(),
        'Namaste Fab',
        'ACTIVE',
        ecommerce_domain_id,
        'APP',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT DO NOTHING
    RETURNING id INTO namaste_fab_id;
    
    -- Insert Kalakosh (APP tenant)
    INSERT INTO tenants (id, name, status, domain_id, type, created_at, updated_at)
    VALUES (
        gen_random_uuid(),
        'Kalakosh',
        'ACTIVE',
        ecommerce_domain_id,
        'APP',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
    ON CONFLICT DO NOTHING
    RETURNING id INTO kalakosh_id;
    
    RAISE NOTICE 'Inserted Namaste Fab and Kalakosh APP tenants for ecommerce domain';
END $$;

