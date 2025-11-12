package com.nexus.ecommerce.fulfilment.scheduler;

import com.nexus.ecommerce.fulfilment.entity.DeliveryConfirmation;
import com.nexus.ecommerce.fulfilment.repository.DeliveryConfirmationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled jobs for delivery confirmations
 * - Process reschedules
 * - Auto-return after 3 attempts
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryScheduler {
    
    private final DeliveryConfirmationRepository confirmationRepository;
    
    /**
     * Process reschedules - runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    @Transactional
    public void processReschedules() {
        log.info("Processing reschedules...");
        
        LocalDateTime now = LocalDateTime.now();
        List<DeliveryConfirmation> confirmations = confirmationRepository.findAll()
            .stream()
            .filter(c -> c.getNextAttemptAt() != null && c.getNextAttemptAt().isBefore(now))
            .filter(c -> c.getRescheduleCount() < 3)
            .filter(c -> c.getConfirmationStatus() == DeliveryConfirmation.ConfirmationStatus.BOTH_UNAVAILABLE)
            .toList();
        
        for (DeliveryConfirmation confirmation : confirmations) {
            log.info("Processing reschedule for confirmation: {}", confirmation.getId());
            // Reset confirmation status for next attempt
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.PENDING);
            confirmation.setAgentConfirmed(false);
            confirmation.setCustomerConfirmed(false);
            confirmation.setAgentMarkedUnavailable(false);
            confirmation.setCustomerMarkedUnavailable(false);
            confirmationRepository.save(confirmation);
        }
        
        log.info("Processed {} reschedules", confirmations.size());
    }
    
    /**
     * Auto-return after 3 failed attempts - runs every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes
    @Transactional
    public void processAutoReturns() {
        log.info("Processing auto-returns...");
        
        List<DeliveryConfirmation> confirmations = confirmationRepository.findAll()
            .stream()
            .filter(c -> c.getRescheduleCount() >= 3)
            .filter(c -> !c.getAutoReturnInitiated())
            .filter(c -> c.getConfirmationStatus() == DeliveryConfirmation.ConfirmationStatus.BOTH_UNAVAILABLE)
            .toList();
        
        for (DeliveryConfirmation confirmation : confirmations) {
            log.info("Initiating auto-return for confirmation: {}", confirmation.getId());
            confirmation.setAutoReturnInitiated(true);
            confirmation.setConfirmationStatus(DeliveryConfirmation.ConfirmationStatus.RETURNED);
            confirmationRepository.save(confirmation);
            
            // TODO: Create return order via Order Service
            // This would trigger a return order creation
        }
        
        log.info("Processed {} auto-returns", confirmations.size());
    }
}

