package com.nexus.shared.iam.constants;

public enum TenantStatus {
    ACTIVE,
    INACTIVE;

    public String getValue() {
        return name();
    }
}
