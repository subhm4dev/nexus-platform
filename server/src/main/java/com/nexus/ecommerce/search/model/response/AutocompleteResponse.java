package com.nexus.ecommerce.search.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Response DTO for autocomplete suggestions
 */
public record AutocompleteResponse(
    List<String> products,
    
    List<String> categories,
    
    List<String> subcategories
) {}

