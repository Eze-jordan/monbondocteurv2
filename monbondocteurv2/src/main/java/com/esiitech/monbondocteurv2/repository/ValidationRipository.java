package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRipository extends JpaRepository<Validation, Long> {

    Optional<Validation> findByCode(String code);
}

