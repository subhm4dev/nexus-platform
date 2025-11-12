package com.nexus.ecommerce.search.service;

import com.nexus.ecommerce.search.model.response.AutocompleteResponse;

import java.util.UUID;

/**
 * Autocomplete Service Interface
 */
public interface AutocompleteService {
    
    /**
     * Get autocomplete suggestions
     */
    AutocompleteResponse getSuggestions(UUID tenantId, String query);
}

