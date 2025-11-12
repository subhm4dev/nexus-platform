-- Add city, state, and country support to locations table
-- Enables city-based filtering for products and inventory

-- Add city column
ALTER TABLE locations ADD COLUMN city VARCHAR(100);

-- Add state column
ALTER TABLE locations ADD COLUMN state VARCHAR(100);

-- Add country column with default 'IN'
ALTER TABLE locations ADD COLUMN country VARCHAR(2) DEFAULT 'IN';

-- Create indexes for city-based queries
CREATE INDEX idx_locations_city ON locations(city, tenant_id);
CREATE INDEX idx_locations_city_state ON locations(city, state, tenant_id);
CREATE INDEX idx_locations_city_tenant ON locations(city, tenant_id) WHERE active = TRUE;

-- Comments
COMMENT ON COLUMN locations.city IS 'City name for location-based filtering (e.g., "Mumbai", "Delhi")';
COMMENT ON COLUMN locations.state IS 'State or province name';
COMMENT ON COLUMN locations.country IS 'Country code (ISO 3166-1 alpha-2, e.g., "IN", "US")';

