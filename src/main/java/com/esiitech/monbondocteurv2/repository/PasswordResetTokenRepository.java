package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
    void deleteByStructureSanitaire_Id(String structureId);
}
