package com.nexus.shared.iam.model.request;

import com.nexus.shared.iam.validation.EmailOrPhoneRequired;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Login request DTO
 * 
 * <p>Supports login with either email OR phone (at least one required).
 * Password is mandatory.
 * 
 * <p>Domain Code behavior:
 * <ul>
 *   <li><b>domainCode:</b> Optional. Specifies which domain context to login to (e.g., "ecommerce", "hospital", "food-delivery").
 *       Defaults to "ecommerce" if not provided. User must belong to this domain (checked via user_domains table).</li>
 * </ul>
 * 
 * <p>If user belongs to multiple domains, they can login to any domain they're registered in.
 * The JWT token will contain the domainId and tenantId for that specific domain context.
 */
@EmailOrPhoneRequired
public record LoginRequest(
    @Email(message = "Email must be valid format")
    String email,
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format (e.g., +919876543210)")
    String phone,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,
    
    /**
     * Domain code: "ecommerce", "hospital", "food-delivery", "travel", etc.
     * Defaults to "ecommerce" if not provided.
     * User must belong to this domain (validated via user_domains table).
     */
    String domainCode
) {
    /**
     * Default domainCode to "ecommerce" if not provided
     */
    public String domainCode() {
        return domainCode != null && !domainCode.isBlank() ? domainCode : "ecommerce";
    }
}

