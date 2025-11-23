package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByUserEmail(String userEmail);
    List<AuditLog> findByAction(String action);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Tri par date
    List<AuditLog> findAllByOrderByTimestampDesc();
}
