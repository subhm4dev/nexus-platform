package com.nexus.shared.iam.repository;

import com.nexus.shared.iam.entity.JwkKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JwkKeyRepository extends JpaRepository<JwkKey, UUID> {
}
