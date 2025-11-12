package com.nexus.ecommerce.search.service;

import java.util.UUID;

/**
 * Search Index Service Interface
 * Maintains search index from Catalog events
 */
public interface SearchIndexService {
    
    /**
     * Create or update search index entry for a product
     */
    void indexProduct(UUID productId, UUID tenantId, String name, String description, String sku, UUID categoryId, UUID subcategoryId, String keywords);
    
    /**
     * Remove product from search index
     */
    void removeProduct(UUID productId, UUID tenantId);
    
    /**
     * Update category in search index
     */
    void updateCategoryIndex(UUID categoryId, UUID tenantId);
}

