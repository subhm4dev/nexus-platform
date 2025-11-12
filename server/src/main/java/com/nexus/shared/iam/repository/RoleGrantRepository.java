package com.nexus.shared.iam.repository;

import com.nexus.shared.iam.entity.RoleGrant;
import com.nexus.shared.iam.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoleGrantRepository extends JpaRepository<RoleGrant, UUID> {
    List<RoleGrant> findAllByUser(UserAccount userAccount);
}
