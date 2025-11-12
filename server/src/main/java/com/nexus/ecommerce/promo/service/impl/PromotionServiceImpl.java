package com.nexus.ecommerce.promo.service.impl;

import com.nexus.libs.error.exception.BusinessException;
import com.nexus.libs.error.model.ErrorCode;
import com.nexus.libs.httpclient.client.ResilientWebClient;
import com.nexus.ecommerce.promo.entity.Coupon;
import com.nexus.ecommerce.promo.entity.Promotion;
import com.nexus.ecommerce.promo.model.request.CouponRequest;
import com.nexus.ecommerce.promo.model.request.CouponValidationRequest;
import com.nexus.ecommerce.promo.model.request.PriceCalculationRequest;
import com.nexus.ecommerce.promo.model.request.PromotionRequest;
import com.nexus.ecommerce.promo.model.response.CouponResponse;
import com.nexus.ecommerce.promo.model.response.PriceCalculationResponse;
import com.nexus.ecommerce.promo.model.response.PromotionResponse;
import com.nexus.ecommerce.promo.repository.CouponRepository;
import com.nexus.ecommerce.promo.repository.PromotionRepository;
import com.nexus.ecommerce.promo.service.PromotionService;
import com.nexus.libs.response.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 * Implementation of PromotionService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PromotionServiceImpl implements PromotionService {
    
    private final PromotionRepository promotionRepository;
    private final CouponRepository couponRepository;
    private final ResilientWebClient resilientWebClient;
    
    @Value("${services.catalog.url:http://localhost:8084}")
    private String catalogServiceUrl;
    
    @Override
    @Transactional
    public PromotionResponse createPromotion(UUID userId, UUID tenantId, List<String> roles, PromotionRequest request) {
        log.debug("Creating promotion: tenantId={}, name={}", tenantId, request.name());
        
        // Authorization check
        if (!hasSellerOrAdminRole(roles)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Only SELLER and ADMIN roles can create promotions");
        }
        
        Promotion promotion = Promotion.builder()
            .tenantId(tenantId)
            .name(request.name())
            .type(request.type())
            .discountType(request.discountType())
            .discountValue(request.discountValue())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .eligibilityCriteria(request.eligibilityCriteria())
            .priority(request.priority() != null ? request.priority() : 0)
            .active(true)
            .build();
        
        Promotion saved = promotionRepository.save(promotion);
        return toPromotionResponse(saved);
    }
    
    @Override
    public List<PromotionResponse> getActivePromotions(UUID productId, UUID tenantId) {
        log.debug("Getting active promotions for product: {}, tenant: {}", productId, tenantId);
        
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotions(tenantId, now);
        
        // Filter by product eligibility (simplified - in production would check eligibility_criteria JSON)
        return promotions.stream()
            .map(this::toPromotionResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public PriceCalculationResponse calculatePrice(UUID tenantId, PriceCalculationRequest request) {
        log.debug("Calculating price: productId={}, quantity={}, couponCode={}", 
            request.productId(), request.quantity(), request.couponCode());
        
        // 1. Fetch base price from Catalog service
        BigDecimal basePrice = fetchProductPrice(request.productId(), tenantId);
        BigDecimal totalBasePrice = basePrice.multiply(BigDecimal.valueOf(request.quantity()));
        
        // 2. Get active promotions
        LocalDateTime now = LocalDateTime.now();
        List<Promotion> promotions = promotionRepository.findActivePromotions(tenantId, now);
        
        // 3. Apply promotions in priority order
        BigDecimal discountAmount = BigDecimal.ZERO;
        List<String> appliedPromotions = new ArrayList<>();
        
        for (Promotion promotion : promotions) {
            BigDecimal promoDiscount = calculatePromotionDiscount(promotion, totalBasePrice);
            if (promoDiscount.compareTo(BigDecimal.ZERO) > 0) {
                discountAmount = discountAmount.add(promoDiscount);
                appliedPromotions.add(promotion.getName());
            }
        }
        
        // 4. Apply coupon if provided
        if (request.couponCode() != null && !request.couponCode().isEmpty()) {
            Optional<Coupon> couponOpt = couponRepository.findByCodeAndTenantId(request.couponCode(), tenantId);
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                if (isCouponValid(coupon, totalBasePrice)) {
                    BigDecimal couponDiscount = calculateCouponDiscount(coupon, totalBasePrice);
                    discountAmount = discountAmount.add(couponDiscount);
                }
            }
        }
        
        // 5. Calculate final price (ensure non-negative)
        BigDecimal finalPrice = totalBasePrice.subtract(discountAmount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) {
            finalPrice = BigDecimal.ZERO;
        }
        
        return new PriceCalculationResponse(
            totalBasePrice,
            discountAmount,
            finalPrice,
            appliedPromotions,
            "USD" // Would get from product
        );
    }
    
    @Override
    @Transactional
    public CouponResponse createCoupon(UUID userId, UUID tenantId, List<String> roles, CouponRequest request) {
        log.debug("Creating coupon: tenantId={}, code={}", tenantId, request.code());
        
        // Authorization check
        if (!hasSellerOrAdminRole(roles)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Only SELLER and ADMIN roles can create coupons");
        }
        
        // Generate code if not provided
        String code = request.code();
        if (code == null || code.isEmpty()) {
            code = generateCouponCode();
        }
        
        // Check code uniqueness
        if (couponRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.SKU_REQUIRED, "Coupon code already exists: " + code);
        }
        
        Coupon coupon = Coupon.builder()
            .tenantId(tenantId)
            .code(code)
            .discountType(request.discountType())
            .discountValue(request.discountValue())
            .usageLimit(request.usageLimit())
            .usedCount(0)
            .expiryDate(request.expiryDate())
            .minOrderValue(request.minOrderValue())
            .active(true)
            .build();
        
        Coupon saved = couponRepository.save(coupon);
        return toCouponResponse(saved);
    }
    
    @Override
    public CouponResponse validateCoupon(UUID tenantId, CouponValidationRequest request) {
        log.debug("Validating coupon: code={}, tenantId={}", request.couponCode(), tenantId);
        
        Coupon coupon = couponRepository.findByCodeAndTenantId(request.couponCode(), tenantId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Invalid coupon code"));
        
        // Validate coupon
        if (!coupon.getActive()) {
            throw new BusinessException(ErrorCode.SKU_REQUIRED, "Coupon is not active");
        }
        
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.SKU_REQUIRED, "Coupon has expired");
        }
        
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BusinessException(ErrorCode.SKU_REQUIRED, "Coupon usage limit exceeded");
        }
        
        if (request.orderTotal() != null && coupon.getMinOrderValue() != null) {
            if (request.orderTotal().compareTo(coupon.getMinOrderValue()) < 0) {
                throw new BusinessException(ErrorCode.SKU_REQUIRED, 
                    "Minimum order value not met. Required: " + coupon.getMinOrderValue());
            }
        }
        
        return toCouponResponse(coupon);
    }
    
    // Helper methods
    
    private BigDecimal fetchProductPrice(UUID productId, UUID tenantId) {
        try {
            WebClient webClient = resilientWebClient.create("catalog-service", catalogServiceUrl);
            
            ApiResponse<?> response = webClient
                .get()
                .uri("/api/v1/product/{id}", productId)
                .header("X-Tenant-Id", tenantId.toString())
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block();
            
            if (response == null || response.data() == null) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
            }
            
            // Extract price from response
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> productMap = (java.util.Map<String, Object>) response.data();
            return new BigDecimal(productMap.get("price").toString());
            
        } catch (WebClientResponseException.NotFound e) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Product not found: " + productId);
        } catch (Exception e) {
            log.error("Error fetching product price", e);
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND, "Failed to fetch product: " + e.getMessage());
        }
    }
    
    private BigDecimal calculatePromotionDiscount(Promotion promotion, BigDecimal basePrice) {
        if ("PERCENTAGE".equals(promotion.getDiscountType())) {
            return basePrice.multiply(promotion.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else if ("FIXED".equals(promotion.getDiscountType())) {
            return promotion.getDiscountValue().min(basePrice); // Don't exceed base price
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateCouponDiscount(Coupon coupon, BigDecimal orderTotal) {
        if ("PERCENTAGE".equals(coupon.getDiscountType())) {
            return orderTotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else if ("FIXED".equals(coupon.getDiscountType())) {
            return coupon.getDiscountValue().min(orderTotal); // Don't exceed order total
        }
        return BigDecimal.ZERO;
    }
    
    private boolean isCouponValid(Coupon coupon, BigDecimal orderTotal) {
        if (!coupon.getActive()) return false;
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) return false;
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) return false;
        if (coupon.getMinOrderValue() != null && orderTotal.compareTo(coupon.getMinOrderValue()) < 0) return false;
        return true;
    }
    
    private String generateCouponCode() {
        return "PROMO" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private boolean hasSellerOrAdminRole(List<String> roles) {
        return roles != null && (roles.contains("SELLER") || roles.contains("ADMIN"));
    }
    
    private PromotionResponse toPromotionResponse(Promotion promotion) {
        return new PromotionResponse(
            promotion.getId(),
            promotion.getTenantId(),
            promotion.getName(),
            promotion.getType(),
            promotion.getDiscountType(),
            promotion.getDiscountValue(),
            promotion.getStartDate(),
            promotion.getEndDate(),
            promotion.getEligibilityCriteria(),
            promotion.getPriority(),
            promotion.getActive(),
            promotion.getCreatedAt(),
            promotion.getUpdatedAt()
        );
    }
    
    private CouponResponse toCouponResponse(Coupon coupon) {
        return new CouponResponse(
            coupon.getId(),
            coupon.getTenantId(),
            coupon.getCode(),
            coupon.getDiscountType(),
            coupon.getDiscountValue(),
            coupon.getUsageLimit(),
            coupon.getUsedCount(),
            coupon.getExpiryDate(),
            coupon.getMinOrderValue(),
            coupon.getActive(),
            coupon.getCreatedAt(),
            coupon.getUpdatedAt()
        );
    }
}

