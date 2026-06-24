package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.enums.PaymentStatus;
import com.esiitech.monbondocteurv2.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    Optional<PaymentTransaction> findByReference(String reference);

    Optional<PaymentTransaction> findByExternalTransactionId(String externalTransactionId);

    boolean existsByStructureIdAndStatusIn(String structureId, Collection<PaymentStatus> statuses);
}