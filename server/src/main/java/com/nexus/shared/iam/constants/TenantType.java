package com.nexus.shared.iam.constants;

/**
 * Tenant Type Enum
 * 
 * <p>Defines the type of tenant in the system with hierarchy: APP → SELLER → BRANCH
 * <ul>
 *   <li><b>APP</b>: Application-level tenant (e.g., Namaste Fab, Kalakosh) - top-level tenant, no parent</li>
 *   <li><b>SELLER</b>: Seller tenant - has parent = APP tenant, created when seller registers</li>
 *   <li><b>BRANCH</b>: Branch/child tenant (e.g., Kalakosh Mumbai, Kalakosh Delhi) - has parent = SELLER tenant</li>
 * </ul>
 * 
 * <p>Hierarchy Structure:
 * <pre>
 * APP (Kalakosh)
 * ├─ SELLER (Seller A) - parent = Kalakosh
 * │   ├─ BRANCH (Mumbai) - parent = Seller A
 * │   └─ BRANCH (Delhi) - parent = Seller A
 * └─ SELLER (Seller B) - parent = Kalakosh
 * </pre>
 */
public enum TenantType {
    /**
     * Application-level tenant (top-level)
     * Examples: Namaste Fab, Kalakosh
     * - No parent tenant (parent_tenant_id = NULL)
     * - Customers register with this tenant
     * - Complete app isolation
     * - Sellers register under this tenant (creates SELLER child tenant)
     */
    APP,
    
    /**
     * Seller tenant
     * - Has parent tenant = APP tenant
     * - Created automatically when seller registers with APP tenant
     * - Seller user belongs to this SELLER tenant
     * - Products belong to this SELLER tenant (via parent_tenant_id)
     * - Can have BRANCH children for multi-branch sellers
     */
    SELLER,
    
    /**
     * Branch/child tenant
     * Examples: Kalakosh Mumbai, Kalakosh Delhi
     * - Has parent tenant = SELLER tenant
     * - Staff belong to branch tenants
     * - Products can be branch-specific (tenant_id = branch)
     * - Sellers at parent SELLER tenant can manage all branches
     */
    BRANCH
}

