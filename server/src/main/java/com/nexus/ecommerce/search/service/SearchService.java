package com.nexus.ecommerce.search.service;

import com.nexus.ecommerce.search.model.response.SearchResponse;

import java.util.UUID;

/**
 * Search Service Interface
 */
public interface SearchService {
    
    /**
     * Fast live search across products, categories, and subcategories
     */
    SearchResponse search(
        UUID tenantId,
        String query,
        UUID categoryId,
        UUID subcategoryId,
        int page,
        int size
    );
    
    /**
     * Search within a category
     */
    SearchResponse searchByCategory(UUID categoryId, UUID tenantId, String query, int page, int size);
    
    /**
     * Search within a subcategory
     */
    SearchResponse searchBySubcategory(UUID subcategoryId, UUID tenantId, String query, int page, int size);
}

