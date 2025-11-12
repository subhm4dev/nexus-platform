package com.nexus.ecommerce.fulfilment.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Service for calculating proximity and distance between GPS coordinates
 */
@Service
public class ProximityService {
    
    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final int DEFAULT_PROXIMITY_RADIUS = 50; // meters
    private static final int MAX_LOCATION_ACCURACY = 10; // meters
    
    /**
     * Calculate distance between two GPS coordinates using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return distance in meters
     */
    public double calculateDistance(
        double lat1, double lon1, 
        double lat2, double lon2
    ) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * 
                   Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distanceKm = EARTH_RADIUS_KM * c;
        
        return distanceKm * 1000; // Convert to meters
    }
    
    /**
     * Check if a point is within proximity radius of a target location
     * @param targetLat Target latitude
     * @param targetLon Target longitude
     * @param pointLat Point latitude
     * @param pointLon Point longitude
     * @param proximityRadius Proximity radius in meters
     * @return true if point is within radius
     */
    public boolean isWithinProximity(
        double targetLat, double targetLon,
        double pointLat, double pointLon,
        int proximityRadius
    ) {
        double distance = calculateDistance(targetLat, targetLon, pointLat, pointLon);
        return distance <= proximityRadius;
    }
    
    /**
     * Check if both agent and customer are in proximity of delivery address
     * @param deliveryLat Delivery address latitude
     * @param deliveryLon Delivery address longitude
     * @param agentLat Agent latitude
     * @param agentLon Agent longitude
     * @param customerLat Customer latitude
     * @param customerLon Customer longitude
     * @param proximityRadius Proximity radius in meters
     * @return true if both are in proximity
     */
    public boolean verifyProximity(
        double deliveryLat, double deliveryLon,
        double agentLat, double agentLon,
        double customerLat, double customerLon,
        int proximityRadius
    ) {
        boolean agentInProximity = isWithinProximity(
            deliveryLat, deliveryLon, agentLat, agentLon, proximityRadius
        );
        boolean customerInProximity = isWithinProximity(
            deliveryLat, deliveryLon, customerLat, customerLon, proximityRadius
        );
        
        return agentInProximity && customerInProximity;
    }
    
    /**
     * Check if agent and customer are close to each other
     * This is the KEY check - parties must be in proximity to each other (anywhere)
     * Not limited to delivery address - they can meet at any location
     * 
     * @param agentLat Agent latitude
     * @param agentLon Agent longitude
     * @param customerLat Customer latitude
     * @param customerLon Customer longitude
     * @param maxDistance Maximum distance in meters (default: 50m)
     * @return true if parties are within maxDistance of each other
     */
    public boolean arePartiesClose(
        double agentLat, double agentLon,
        double customerLat, double customerLon,
        int maxDistance
    ) {
        double distance = calculateDistance(agentLat, agentLon, customerLat, customerLon);
        return distance <= maxDistance;
    }
    
    /**
     * Verify proximity between parties and record actual delivery location
     * This supports flexible delivery - parties can meet anywhere, not just at delivery address
     * 
     * @param agentLat Agent latitude
     * @param agentLon Agent longitude
     * @param customerLat Customer latitude
     * @param customerLon Customer longitude
     * @param scheduledDeliveryLat Scheduled delivery address latitude
     * @param scheduledDeliveryLon Scheduled delivery address longitude
     * @param proximityRadius Proximity radius in meters (default: 50m)
     * @return ProximityResult with verification status and location details
     */
    public ProximityResult verifyAndRecordLocation(
        double agentLat, double agentLon,
        double customerLat, double customerLon,
        double scheduledDeliveryLat, double scheduledDeliveryLon,
        int proximityRadius
    ) {
        // KEY CHECK: Are parties close to each other? (anywhere)
        boolean partiesInProximity = arePartiesClose(
            agentLat, agentLon, customerLat, customerLon, proximityRadius
        );
        
        // Calculate distance between parties
        double distanceBetweenParties = calculateDistance(
            agentLat, agentLon, customerLat, customerLon
        );
        
        // Calculate distance from scheduled address (for records)
        double actualLat = (agentLat + customerLat) / 2; // Midpoint
        double actualLon = (agentLon + customerLon) / 2;
        double distanceFromScheduled = calculateDistance(
            scheduledDeliveryLat, scheduledDeliveryLon, actualLat, actualLon
        );
        
        // Determine location type
        String locationType = distanceFromScheduled > 100 ? 
            "ALTERNATE_LOCATION" : "SCHEDULED_ADDRESS";
        
        return new ProximityResult(
            partiesInProximity,
            roundDistance(distanceBetweenParties),
            roundDistance(distanceFromScheduled),
            locationType,
            BigDecimal.valueOf(actualLat),
            BigDecimal.valueOf(actualLon)
        );
    }
    
    /**
     * Result of proximity verification
     */
    public record ProximityResult(
        boolean partiesInProximity,      // KEY: Are parties close to each other?
        BigDecimal distanceBetweenParties, // Distance between agent and customer
        BigDecimal distanceFromScheduled,   // Distance from scheduled address
        String locationType,               // SCHEDULED_ADDRESS or ALTERNATE_LOCATION
        BigDecimal actualDeliveryLat,      // Actual delivery latitude
        BigDecimal actualDeliveryLon       // Actual delivery longitude
    ) {}
    
    /**
     * Validate location accuracy
     * @param accuracy Location accuracy in meters
     * @return true if accuracy is acceptable
     */
    public boolean isValidLocationAccuracy(double accuracy) {
        return accuracy > 0 && accuracy <= MAX_LOCATION_ACCURACY;
    }
    
    /**
     * Round distance to 2 decimal places
     */
    public BigDecimal roundDistance(double distance) {
        return BigDecimal.valueOf(distance)
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Get default proximity radius
     */
    public int getDefaultProximityRadius() {
        return DEFAULT_PROXIMITY_RADIUS;
    }
    
    /**
     * Get max acceptable location accuracy
     */
    public int getMaxLocationAccuracy() {
        return MAX_LOCATION_ACCURACY;
    }
    
    /**
     * Check if agent is in proximity with ANY of the alternate recipients
     * This is used when customer is not available and alternate recipient can receive order
     * 
     * @param agentLat Agent latitude
     * @param agentLon Agent longitude
     * @param alternateRecipients List of alternate recipient locations (lat, lon pairs)
     * @param proximityRadius Proximity radius in meters (default: 50m)
     * @return ProximityMatchResult with match status and matched recipient index
     */
    public ProximityMatchResult checkProximityWithAlternates(
        double agentLat, double agentLon,
        java.util.List<AlternateRecipientLocation> alternateRecipients,
        int proximityRadius
    ) {
        if (alternateRecipients == null || alternateRecipients.isEmpty()) {
            return new ProximityMatchResult(false, -1, null, null);
        }
        
        // Check proximity with each alternate recipient
        for (int i = 0; i < alternateRecipients.size(); i++) {
            AlternateRecipientLocation alt = alternateRecipients.get(i);
            if (alt.latitude() != null && alt.longitude() != null) {
                double distance = calculateDistance(
                    agentLat, agentLon,
                    alt.latitude().doubleValue(), alt.longitude().doubleValue()
                );
                
                if (distance <= proximityRadius) {
                    return new ProximityMatchResult(
                        true,
                        i,
                        roundDistance(distance),
                        alt
                    );
                }
            }
        }
        
        return new ProximityMatchResult(false, -1, null, null);
    }
    
    /**
     * Verify proximity between agent and alternate recipient
     * 
     * @param agentLat Agent latitude
     * @param agentLon Agent longitude
     * @param alternateLat Alternate recipient latitude
     * @param alternateLon Alternate recipient longitude
     * @param proximityRadius Proximity radius in meters
     * @return ProximityResult with verification status
     */
    public ProximityResult verifyAlternateRecipientProximity(
        double agentLat, double agentLon,
        double alternateLat, double alternateLon,
        double scheduledDeliveryLat, double scheduledDeliveryLon,
        int proximityRadius
    ) {
        return verifyAndRecordLocation(
            agentLat, agentLon,
            alternateLat, alternateLon,
            scheduledDeliveryLat, scheduledDeliveryLon,
            proximityRadius
        );
    }
    
    /**
     * Result of checking proximity with alternate recipients
     */
    public record ProximityMatchResult(
        boolean matched,                          // Is agent in proximity with any alternate?
        int matchedIndex,                         // Index of matched alternate recipient
        BigDecimal distance,                      // Distance to matched alternate
        AlternateRecipientLocation matchedRecipient // Matched alternate recipient details
    ) {}
    
    /**
     * Alternate recipient location data
     */
    public record AlternateRecipientLocation(
        UUID recipientId,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal locationAccuracy,
        String name,
        String phoneNumber
    ) {}
}

