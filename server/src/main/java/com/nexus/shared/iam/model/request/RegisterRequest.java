package com.nexus.shared.iam.model.request;

import com.nexus.shared.iam.constants.Role;
import com.nexus.shared.iam.validation.EmailOrPhoneRequired;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Registration request DTO
 * 
 * <p>Supports registration with either email OR phone (at least one required).
 * Password and role are mandatory fields.
 * 
 * <p>Domain Code behavior:
 * <ul>
 *   <li><b>domainCode:</b> REQUIRED. Specifies which domain to register in (e.g., "ecommerce", "hospital", "food-delivery").
 *       Must be provided explicitly. No default value.</li>
 * </ul>
 * 
 * <p>Tenant ID behavior (REQUIRED for all roles):
 * <ul>
 *   <li><b>CUSTOMER:</b> tenantId is REQUIRED. Must be an APP tenant (e.g., Namaste Fab or Kalakosh). User is assigned directly to this APP tenant.</li>
 *   <li><b>SELLER:</b> tenantId is REQUIRED. Must be an APP tenant (e.g., Kalakosh). Backend creates a new SELLER tenant with parent = provided APP tenant. Seller user is assigned to the newly created SELLER tenant.</li>
 *   <li><b>STAFF:</b> tenantId is REQUIRED. Must be a SELLER or BRANCH tenant. Staff user is assigned directly to the provided tenant (no new tenant creation).</li>
 *   <li><b>Other roles:</b> tenantId is REQUIRED. Must belong to the specified domain.</li>
 * </ul>
 * 
 * <p>Tenant hierarchy: APP → SELLER → BRANCH
 * <ul>
 *   <li>APP: Top-level tenant (Namaste Fab, Kalakosh) - no parent</li>
 *   <li>SELLER: Created when seller registers with APP tenant - parent = APP</li>
 *   <li>BRANCH: Created for multi-branch sellers - parent = SELLER</li>
 * </ul>
 * 
 * <p>If tenantId is provided, it must belong to the specified domain (validated).
 */
@EmailOrPhoneRequired
public record RegisterRequest(
    @Email(message = "Email must be valid format")
    String email,
    
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format (e.g., +919876543210)")
    String phone,
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password,
    
    /**
     * Domain code: "ecommerce", "hospital", "food-delivery", "travel", etc.
     * REQUIRED - must be provided explicitly.
     */
    @NotBlank(message = "Domain code is required")
    String domainCode,
    
    /**
     * Tenant ID: REQUIRED for all roles.
     * - CUSTOMER: Must be an APP tenant (e.g., Namaste Fab or Kalakosh)
     * - SELLER: Must be an APP tenant (backend creates SELLER tenant with parent = APP)
     * - STAFF: Must be a SELLER or BRANCH tenant
     */
    @NotNull(message = "Tenant ID is required")
    UUID tenantId,
    
    @NotNull(message = "Role is required")
    Role role
) {
}
