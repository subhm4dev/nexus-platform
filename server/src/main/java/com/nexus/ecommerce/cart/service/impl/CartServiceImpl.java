package com.nexus.ecommerce.cart.service.impl;

import com.nexus.ecommerce.cart.model.Cart;
import com.nexus.ecommerce.cart.model.request.AddItemRequest;
import com.nexus.ecommerce.cart.model.request.CouponRequest;
import com.nexus.ecommerce.cart.model.request.UpdateItemRequest;
import com.nexus.ecommerce.cart.model.response.CartItemResponse;
import com.nexus.ecommerce.cart.model.response.CartResponse;
import com.nexus.ecommerce.cart.repository.CartRepository;
import com.nexus.shared.security.JwtAuthenticationToken;
import com.nexus.ecommerce.cart.service.CartService;
import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.httpclient.client.ResilientWebClient;
import com.nexus.libs.response.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CartService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {
    
    private final CartRepository cartRepository;
    private final ResilientWebClient resilientWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${services.catalog.url:http://localhost:8084}")
    private String catalogServiceUrl;
    
    @Value("${services.inventory.url:http://localhost:8085}")
    private String inventoryServiceUrl;
    
    @Value("${services.promo.url:http://localhost:8086}")
    private String promoServiceUrl;
    
    private static final String CART_ITEM_NOT_FOUND = "Cart item not found";
    
    @Override
    public CartResponse addItem(UUID userId, UUID tenantId, AddItemRequest request) {
        log.debug("Adding item to cart: userId={}, productId={}, quantity={}", 
            userId, request.productId(), request.quantity());
        
        // 1. Fetch product details from Catalog service
        ProductInfo productInfo = fetchProductFromCatalog(request.productId(), tenantId);
        
        // 2. Check inventory availability
        checkInventoryAvailability(productInfo.sku(), tenantId, request.quantity());
        
        // 3. Calculate price with promotions (for now, use base price; promotion service will be integrated later)
        BigDecimal unitPrice = calculatePriceWithPromotions(productInfo.productId(), productInfo.price(), request.quantity(), null);
        
        // 4. Get or create cart
        Cart cart = cartRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElse(createNewCart(userId, tenantId, productInfo.currency()));
        
        // 5. Add or update cart item
        addOrUpdateCartItem(cart, request.productId(), productInfo, request.quantity(), unitPrice, request.variantId());
        
        // 6. Recalculate cart totals
        recalculateCartTotals(cart);
        
        // 7. Save cart to Redis
        cartRepository.save(cart);
        
        return toResponse(cart);
    }
    
    @Override
    public CartResponse updateItem(UUID userId, UUID tenantId, String itemId, UpdateItemRequest request) {
        log.debug("Updating cart item: userId={}, itemId={}, newQuantity={}", userId, itemId, request.quantity());
        
        // 1. Get cart
        Cart cart = cartRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Cart not found"));
        
        // 2. Find cart item
        Cart.CartItem cartItem = cart.getItems().stream()
            .filter(item -> item.getItemId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, CART_ITEM_NOT_FOUND));
        
        // 3. If quantity increased, check inventory
        if (request.quantity() > cartItem.getQuantity()) {
            int additionalQuantity = request.quantity() - cartItem.getQuantity();
            checkInventoryAvailability(cartItem.getSku(), tenantId, additionalQuantity);
        }
        
        // 4. Update quantity and recalculate item total
        cartItem.setQuantity(request.quantity());
        cartItem.setTotalPrice(cartItem.getUnitPrice().multiply(BigDecimal.valueOf(request.quantity())));
        
        // 5. Recalculate cart totals
        recalculateCartTotals(cart);
        
        // 6. Save cart
        cartRepository.save(cart);
        
        return toResponse(cart);
    }
    
    @Override
    public CartResponse removeItem(UUID userId, UUID tenantId, String itemId) {
        log.debug("Removing cart item: userId={}, itemId={}", userId, itemId);
        
        // 1. Get cart
        Cart cart = cartRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Cart not found"));
        
        // 2. Remove item
        boolean removed = cart.getItems().removeIf(item -> item.getItemId().equals(itemId));
        if (!removed) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, CART_ITEM_NOT_FOUND);
        }
        
        // 3. If cart is empty, delete it; otherwise recalculate totals
        if (cart.getItems().isEmpty()) {
            cartRepository.delete(tenantId, userId);
            return createEmptyCartResponse(userId, tenantId);
        } else {
            recalculateCartTotals(cart);
            cartRepository.save(cart);
            return toResponse(cart);
        }
    }
    
    @Override
    public CartResponse getCart(UUID userId, UUID tenantId) {
        log.debug("Getting cart: userId={}, tenantId={}", userId, tenantId);
        
        Optional<Cart> cartOpt = cartRepository.findByTenantIdAndUserId(tenantId, userId);
        
        if (cartOpt.isEmpty()) {
            return createEmptyCartResponse(userId, tenantId);
        }
        
        Cart cart = cartOpt.get();
        
        // Enrich items with current product details and prices
        enrichCartItems(cart, tenantId);
        
        // Recalculate totals with latest promotions
        recalculateCartTotals(cart);
        
        // Save updated cart
        cartRepository.save(cart);
        
        return toResponse(cart);
    }
    
    @Override
    public void clearCart(UUID userId, UUID tenantId) {
        log.debug("Clearing cart: userId={}, tenantId={}", userId, tenantId);
        cartRepository.delete(tenantId, userId);
    }
    
    @Override
    public CartResponse applyCoupon(UUID userId, UUID tenantId, CouponRequest request) {
        log.debug("Applying coupon: userId={}, couponCode={}", userId, request.couponCode());
        
        // 1. Get cart
        Cart cart = cartRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Cart not found"));
        
        // 2. Validate coupon via Promotion service
        validateCoupon(request.couponCode(), tenantId, cart);
        
        // 3. Store coupon code
        cart.setCouponCode(request.couponCode());
        
        // 4. Recalculate totals with coupon
        recalculateCartTotals(cart);
        
        // 5. Save cart
        cartRepository.save(cart);
        
        return toResponse(cart);
    }
    
    @Override
    public CartResponse removeCoupon(UUID userId, UUID tenantId) {
        log.debug("Removing coupon: userId={}", userId);
        
        // 1. Get cart
        Cart cart = cartRepository.findByTenantIdAndUserId(tenantId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Cart not found"));
        
        // 2. Remove coupon code
        cart.setCouponCode(null);
        
        // 3. Recalculate totals without coupon
        recalculateCartTotals(cart);
        
        // 4. Save cart
        cartRepository.save(cart);
        
        return toResponse(cart);
    }
    
    // Helper methods
    
    private ProductInfo fetchProductFromCatalog(UUID productId, UUID tenantId) {
        try {
            WebClient webClient = resilientWebClient.create("catalog-service", catalogServiceUrl);
            String token = getJwtToken();
            
            // Build request with conditional Authorization header
            // Public endpoints (GET /api/v1/product/{id}) don't require authentication
            // Only send token if it exists (for service-to-service calls with user context)
            // Catalog service expects tenantId as query parameter for public access
            WebClient.RequestHeadersSpec<?> requestSpec = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                    .path("/api/v1/product/{id}")
                    .queryParam("tenantId", tenantId.toString())
                    .build(productId))
                .header("X-Tenant-Id", tenantId.toString());
            
            // Only add Authorization header if token exists and is not null/empty
            if (token != null && !token.isEmpty()) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + token);
            }
            
            ApiResponse<ProductInfo> response = requestSpec
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();
            
            if (response == null || response.data() == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
            }
            
            // Convert response data to ProductInfo
            // ProductResponse uses @JsonProperty annotations, so we need to map it properly
            Object productData = response.data();
            if (productData instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> productMap = (java.util.Map<String, Object>) productData;
                return new ProductInfo(
                    UUID.fromString(productMap.get("product_id").toString()),
                    (String) productMap.get("name"),
                    (String) productMap.get("sku"),
                    new BigDecimal(productMap.get("price").toString()),
                    (String) productMap.get("currency"),
                    UUID.fromString(productMap.get("category_id").toString()),
                    UUID.fromString(productMap.get("seller_id").toString()),
                    (List<String>) productMap.getOrDefault("images", new ArrayList<>()),
                    (String) productMap.get("status")
                );
            }
            return objectMapper.convertValue(productData, ProductInfo.class);
            
        } catch (WebClientResponseException.NotFound e) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        } catch (Exception e) {
            log.error("Error fetching product from catalog service", e);
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Failed to fetch product: " + e.getMessage());
        }
    }
    
    private void checkInventoryAvailability(String sku, UUID tenantId, Integer quantity) {
        try {
            WebClient webClient = resilientWebClient.create("inventory-service", inventoryServiceUrl);
            String token = getJwtToken();
            
            // Get first available location for this SKU
            // For now, we'll use a simplified check - in production, you'd get the default location
            // This is a placeholder - actual implementation would get locations with stock
            log.debug("Checking inventory for SKU: {}, quantity: {}", sku, quantity);
            
            // TODO: Implement proper inventory check once location management is available
            // For now, we'll skip the check to allow cart operations
            
        } catch (Exception e) {
            log.warn("Inventory check failed, proceeding anyway: {}", e.getMessage());
            // In a real scenario, you might want to fail here or use a fallback
        }
    }
    
    private BigDecimal calculatePriceWithPromotions(UUID productId, BigDecimal basePrice, Integer quantity, String couponCode) {
        // For now, return base price
        // Once promotion service is implemented, this will call:
        // POST /api/v1/promotion/calculate with productId, quantity, couponCode
        return basePrice;
    }
    
    private Cart createNewCart(UUID userId, UUID tenantId, String currency) {
        return Cart.builder()
            .userId(userId)
            .tenantId(tenantId)
            .items(new ArrayList<>())
            .subtotal(BigDecimal.ZERO)
            .discountAmount(BigDecimal.ZERO)
            .taxAmount(BigDecimal.ZERO)
            .shippingCost(BigDecimal.ZERO)
            .total(BigDecimal.ZERO)
            .currency(currency)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    private void addOrUpdateCartItem(Cart cart, UUID productId, ProductInfo productInfo, 
                                     Integer quantity, BigDecimal unitPrice, UUID variantId) {
        // Check if item already exists (by productId and variantId)
        Optional<Cart.CartItem> existingItem = cart.getItems().stream()
            .filter(item -> item.getProductId().equals(productId) && 
                   (variantId == null ? item.getVariantId() == null : variantId.equals(item.getVariantId())))
            .findFirst();
        
        if (existingItem.isPresent()) {
            // Update existing item
            Cart.CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        } else {
            // Add new item
            Cart.CartItem newItem = Cart.CartItem.builder()
                .itemId(UUID.randomUUID().toString())
                .productId(productId)
                .sku(productInfo.sku())
                .name(productInfo.name())
                .imageUrl(productInfo.images() != null && !productInfo.images().isEmpty() 
                    ? productInfo.images().get(0) : null)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
                .variantId(variantId)
                .currency(productInfo.currency())
                .build();
            
            cart.getItems().add(newItem);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
    }
    
    private void recalculateCartTotals(Cart cart) {
        // Calculate subtotal (sum of all item totals)
        BigDecimal subtotal = cart.getItems().stream()
            .map(Cart.CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        cart.setSubtotal(subtotal);
        
        // Calculate discount (from promotions + coupon)
        // For now, discount is 0; will be calculated when promotion service is integrated
        BigDecimal discountAmount = BigDecimal.ZERO;
        
        // Apply coupon discount if coupon code exists
        if (cart.getCouponCode() != null && !cart.getCouponCode().isEmpty()) {
            // TODO: Calculate coupon discount via Promotion service
            // For now, discount remains 0
        }
        
        cart.setDiscountAmount(discountAmount);
        
        // Calculate tax (placeholder - would be calculated based on address, etc.)
        cart.setTaxAmount(BigDecimal.ZERO);
        
        // Shipping cost is calculated during checkout
        cart.setShippingCost(BigDecimal.ZERO);
        
        // Calculate final total
        BigDecimal total = subtotal
            .subtract(discountAmount)
            .add(cart.getTaxAmount())
            .add(cart.getShippingCost());
        
        cart.setTotal(total);
        cart.setUpdatedAt(LocalDateTime.now());
    }
    
    private void enrichCartItems(Cart cart, UUID tenantId) {
        // Enrich each item with current product details
        for (Cart.CartItem item : cart.getItems()) {
            try {
                ProductInfo productInfo = fetchProductFromCatalog(item.getProductId(), tenantId);
                
                // Update item details if product info changed
                item.setName(productInfo.name());
                if (productInfo.images() != null && !productInfo.images().isEmpty()) {
                    item.setImageUrl(productInfo.images().get(0));
                }
                
                // Recalculate price with current promotions
                BigDecimal newUnitPrice = calculatePriceWithPromotions(
                    item.getProductId(), productInfo.price(), item.getQuantity(), cart.getCouponCode());
                
                if (!newUnitPrice.equals(item.getUnitPrice())) {
                    item.setUnitPrice(newUnitPrice);
                    item.setTotalPrice(newUnitPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
                }
                
            } catch (Exception e) {
                log.warn("Failed to enrich cart item {}: {}", item.getItemId(), e.getMessage());
                // Continue with existing item data
            }
        }
    }
    
    private void validateCoupon(String couponCode, UUID tenantId, Cart cart) {
        try {
            WebClient webClient = resilientWebClient.create("promo-service", promoServiceUrl);
            String token = getJwtToken();
            
            // TODO: Call promotion service to validate coupon
            // POST /api/v1/promotion/coupon/validate
            // For now, we'll just store the coupon code
            
            log.debug("Coupon validation placeholder for code: {}", couponCode);
            
        } catch (Exception e) {
            log.error("Error validating coupon", e);
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid coupon: " + e.getMessage());
        }
    }
    
    private String getJwtToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken();
        }
        return null;
    }
    
    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
            .map(item -> new CartItemResponse(
                item.getItemId(),
                item.getProductId(),
                item.getSku(),
                item.getName(),
                item.getImageUrl(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                item.getVariantId(),
                item.getCurrency()
            ))
            .collect(Collectors.toList());
        
        return new CartResponse(
            cart.getUserId(),
            cart.getTenantId(),
            items,
            cart.getCouponCode(),
            cart.getSubtotal(),
            cart.getDiscountAmount(),
            cart.getTaxAmount(),
            cart.getShippingCost(),
            cart.getTotal(),
            cart.getCurrency(),
            cart.getCreatedAt(),
            cart.getUpdatedAt()
        );
    }
    
    private CartResponse createEmptyCartResponse(UUID userId, UUID tenantId) {
        return new CartResponse(
            userId,
            tenantId,
            new ArrayList<>(),
            null,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            "USD",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }
    
    // Inner class for product information (matches ProductResponse structure)
    private record ProductInfo(
        @com.fasterxml.jackson.annotation.JsonProperty("product_id") UUID productId,
        String name,
        String sku,
        BigDecimal price,
        String currency,
        @com.fasterxml.jackson.annotation.JsonProperty("category_id") UUID categoryId,
        @com.fasterxml.jackson.annotation.JsonProperty("seller_id") UUID sellerId,
        List<String> images,
        String status
    ) {}
}

