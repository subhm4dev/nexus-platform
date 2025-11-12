-- Insert default domains
-- This migration inserts the core business domains into the domains table
-- Note: This is the ONLY migration with INSERT statements - all others are DDL only

INSERT INTO domains (id, code, name, description, enabled) VALUES
    -- Core domains
    (gen_random_uuid(), 'ecommerce', 'E-commerce', 'Online marketplace for buying and selling products', TRUE),
    (gen_random_uuid(), 'healthcare', 'Healthcare', 'Hospital and healthcare management system', TRUE),
    (gen_random_uuid(), 'travel', 'Travel & Tourism', 'Travel booking for flights, trains, and buses (intercity)', TRUE),
    (gen_random_uuid(), 'event', 'Events', 'Event management and ticketing platform', TRUE),
    
    -- Food and delivery domains
    (gen_random_uuid(), 'food-delivery', 'Food Delivery', 'Restaurant food delivery services (Zomato, Swiggy)', TRUE),
    (gen_random_uuid(), 'quick-commerce', 'Quick Commerce', 'Hyperlocal quick commerce and instant delivery (Blinkit, Zepto, Instamart)', TRUE),
    (gen_random_uuid(), 'alcohol-delivery', 'Alcohol Delivery', 'Alcohol delivery with age verification and compliance', TRUE),
    
    -- Rental and transportation domains
    (gen_random_uuid(), 'rental', 'Rental Services', 'Property, furniture, and appliance rentals (houses, PGs, furniture, appliances)', TRUE),
    (gen_random_uuid(), 'transportation', 'Transportation', 'Ride-hailing and mobility services (Ola, Uber, Rapido)', TRUE)
ON CONFLICT (code) DO NOTHING;
