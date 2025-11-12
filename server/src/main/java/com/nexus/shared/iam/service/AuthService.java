package com.nexus.shared.iam.service;

import com.nexus.shared.iam.model.request.LoginRequest;
import com.nexus.shared.iam.model.request.LogoutRequest;
import com.nexus.shared.iam.model.request.RefreshRequest;
import com.nexus.shared.iam.model.request.RegisterRequest;
import com.nexus.shared.iam.model.response.LoginResponse;
import com.nexus.shared.iam.model.response.RefreshResponse;
import com.nexus.shared.iam.model.response.RegisterResponse;

import java.util.UUID;

public interface AuthService {

    RegisterResponse register(RegisterRequest registerRequest);
    
    /**
     * Authenticate user and issue JWT tokens
     * 
     * @param loginRequest Contains email/phone and password
     * @return LoginResponse with access token, refresh token, and user info
     */
    LoginResponse login(LoginRequest loginRequest);
    
    /**
     * Refresh access token using refresh token
     * 
     * @param refreshRequest Contains refresh token
     * @param accessToken Optional access token for validation (may be expired)
     * @return RefreshResponse with new access token
     */
    RefreshResponse refresh(RefreshRequest refreshRequest, String accessToken);
    
    /**
     * Logout user by revoking refresh token and blacklisting access token
     * 
     * @param logoutRequest Contains refresh token to revoke
     * @param accessToken Current access token to blacklist (optional, from Authorization header)
     */
    void logout(LogoutRequest logoutRequest, String accessToken);
    
    /**
     * Logout user from all devices
     * Revokes all refresh tokens and blacklists all access tokens for the user
     * 
     * @param userId User ID from authenticated token
     * @param accessToken Current access token to extract user info
     */
    void logoutAll(UUID userId, String accessToken);
}
