package com.nexus.shared.payment.config;

import com.nexus.shared.payment.gateway.PaymentGateway;
import com.nexus.shared.payment.gateway.impl.RazorpayGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Payment Gateway Configuration
 * 
 * <p>Configures payment gateway implementation. Currently uses Razorpay.
 * To switch to another gateway, change the @Primary bean.
 */
@Configuration
public class GatewayConfig {
    
    @Bean
    @Primary
    public PaymentGateway paymentGateway(RazorpayGateway razorpayGateway) {
        return razorpayGateway;
    }
}

