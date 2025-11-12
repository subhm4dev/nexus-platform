package com.nexus.shared.iam.repository;

import com.nexus.shared.iam.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {

    Optional<UserAccount> findByEmail(String email);
    Optional<UserAccount> findByPhone(String phoneNumber);
    Optional<UserAccount> findByEmailOrPhone(String email, String phoneNumber);

}
