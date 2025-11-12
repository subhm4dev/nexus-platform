package com.nexus.shared.iam.model.response;

/**
 * Refresh token response DTO
 */
public record RefreshResponse(
    String accessToken,
    Long expiresIn // seconds until access token expires
) {
}

