package com.nexus.libs.tenant;

import io.opentelemetry.api.baggage.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;

/**
 * Tenant Context - Java 21 compatible implementation using SecurityContextHolder
 * 
 * <p>Replaces Java 25 ScopedValue with Spring Security's SecurityContextHolder
 * which is thread-safe and works with virtual threads in Java 21+.
 * 
 * <p>Extracts tenant and user information from JwtAuthenticationToken
 * stored in Spring Security context. Uses reflection to avoid compile-time
 * dependency on backend module classes.
 */
public final class TenantContext {
  
  /**
   * Get tenant ID from current security context
   * @return tenant ID or null if not available
   */
  public static String tenant() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      try {
        // Use reflection to call getTenantId() on any JwtAuthenticationToken implementation
        // This avoids compile-time dependency on backend module classes
        Method getTenantId = authentication.getClass().getMethod("getTenantId");
        Object tenantId = getTenantId.invoke(authentication);
        return tenantId != null ? tenantId.toString() : null;
      } catch (Exception e) {
        // Not a JwtAuthenticationToken or method doesn't exist
        return null;
      }
    }
    return null;
  }
  
  /**
   * Get user ID from current security context
   * @return user ID or null if not available
   */
  public static String user() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null) {
      try {
        // Use reflection to call getUserId() on any JwtAuthenticationToken implementation
        // This avoids compile-time dependency on backend module classes
        Method getUserId = authentication.getClass().getMethod("getUserId");
        Object userId = getUserId.invoke(authentication);
        return userId != null ? userId.toString() : null;
      } catch (Exception e) {
        // Not a JwtAuthenticationToken or method doesn't exist
        return null;
      }
    }
    return null;
  }
  
  /**
   * Run task with tenant and user context
   * 
   * <p>Note: In modulith architecture, tenant/user context is already
   * available via SecurityContextHolder from JWT authentication filter.
   * This method maintains backward compatibility and sets OpenTelemetry baggage.
   * 
   * @param tenant tenant ID
   * @param user user ID
   * @param task task to run
   */
  public static void runWith(String tenant, String user, Runnable task) {
    // Set OpenTelemetry baggage for observability
    Baggage.current().toBuilder()
           .put("tenant.id", tenant)
           .put("user.id", user).build().makeCurrent();
    task.run();
  }
}

