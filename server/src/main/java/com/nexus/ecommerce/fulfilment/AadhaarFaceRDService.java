package com.nexus.ecommerce.fulfilment.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service for Aadhaar Face RD (Registered Device) integration
 * Official UIDAI service for face authentication
 */
public interface AadhaarFaceRDService {
    
    /**
     * Initiate Aadhaar Face Authentication
     * Step 1: Generate OTP and send to registered mobile
     * 
     * @param aadhaarNumber 12-digit Aadhaar number
     * @return OTP reference ID and transaction ID
     */
    AadhaarOTPResponse initiateFaceAuth(String aadhaarNumber);
    
    /**
     * Verify OTP and perform face authentication
     * Step 2: Verify OTP, capture face, and authenticate
     * 
     * @param aadhaarNumber 12-digit Aadhaar number
     * @param otp OTP received on registered mobile
     * @param otpReferenceId Reference ID from initiateFaceAuth
     * @param transactionId Transaction ID from initiateFaceAuth
     * @param faceImage Base64 encoded live face image from camera
     * @return Authentication result with demographic data
     */
    AadhaarFaceAuthResult verifyFaceAuth(
        String aadhaarNumber,
        String otp,
        String otpReferenceId,
        String transactionId,
        String faceImage // Base64 encoded
    );
    
    /**
     * Calculate age from date of birth
     */
    int calculateAge(LocalDate dateOfBirth);
    
    /**
     * Verify if age meets minimum requirement
     */
    boolean verifyAge(int age, int minimumAge);
    
    /**
     * Response for OTP generation
     */
    record AadhaarOTPResponse(
        String otpReferenceId,
        String transactionId,
        String status, // OTP_SENT, FAILED
        String errorMessage
    ) {}
    
    /**
     * Result of face authentication
     */
    record AadhaarFaceAuthResult(
        boolean authenticated,
        String status, // AUTHENTICATION_SUCCESS, FAILED, OTP_VERIFICATION_FAILED
        String errorCode,
        String errorMessage,
        double matchScore, // 0-100
        DemographicData demographicData,
        LocalDateTime verifiedAt
    ) {}
    
    /**
     * Demographic data from Aadhaar
     */
    record DemographicData(
        String name,
        LocalDate dateOfBirth,
        String gender,
        String address, // If authorized
        String photo, // Base64 encoded photo
        String referenceId // UIDAI reference ID
    ) {}
}

