package com.nexus.shared.iam.constants;

public enum Role {
    // Ecommerce roles
    CUSTOMER,
    SELLER,
    ADMIN,
    STAFF,
    DRIVER,
    // Healthcare roles
    DOCTOR,
    PATIENT,
    RECEPTIONIST;

    public String getValue() {
        return name();
    }
}
