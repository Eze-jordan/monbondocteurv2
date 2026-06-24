package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Formules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulesRepository extends JpaRepository<Formules, String> {

    List<Formules> findByActifTrue();
}