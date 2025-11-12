-- Search Service Schema
-- Consolidated migration: Creates search_index, search_history, and recommendations tables

-- Create search_index table for fast full-text search
CREATE TABLE search_index (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    sku VARCHAR(100),
    category_id UUID,
    subcategory_id UUID,
    keywords TEXT,  -- Pre-computed searchable keywords
    search_vector tsvector,  -- Full-text search vector
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for search_index table
CREATE INDEX idx_search_index_tenant_id ON search_index(tenant_id);
CREATE INDEX idx_search_index_product_id ON search_index(product_id);
CREATE INDEX idx_search_index_category_id ON search_index(category_id);
CREATE INDEX idx_search_index_subcategory_id ON search_index(subcategory_id);
CREATE INDEX idx_search_index_sku ON search_index(sku);

-- Create GIN index for full-text search (PostgreSQL)
CREATE INDEX idx_search_index_search_vector ON search_index USING GIN(search_vector);

-- Create trigger to automatically update search_vector
CREATE OR REPLACE FUNCTION update_search_vector() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', COALESCE(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.description, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.sku, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.keywords, '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER search_vector_update
    BEFORE INSERT OR UPDATE ON search_index
    FOR EACH ROW
    EXECUTE FUNCTION update_search_vector();

-- Create search_history table for analytics
CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    tenant_id UUID NOT NULL,
    query VARCHAR(500) NOT NULL,
    results_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for search_history table
CREATE INDEX idx_search_history_user_id ON search_history(user_id);
CREATE INDEX idx_search_history_tenant_id ON search_history(tenant_id);
CREATE INDEX idx_search_history_created_at ON search_history(created_at);
CREATE INDEX idx_search_history_query ON search_history(query);

-- Create recommendations table
CREATE TABLE recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID,
    product_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    score DECIMAL(10, 4) NOT NULL DEFAULT 0.0,
    type VARCHAR(50) NOT NULL,  -- COLLABORATIVE, TRENDING, SIMILAR, etc.
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for recommendations table
CREATE INDEX idx_recommendations_user_id ON recommendations(user_id);
CREATE INDEX idx_recommendations_product_id ON recommendations(product_id);
CREATE INDEX idx_recommendations_tenant_id ON recommendations(tenant_id);
CREATE INDEX idx_recommendations_type ON recommendations(type);
CREATE INDEX idx_recommendations_score ON recommendations(score DESC);

