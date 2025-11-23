package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Paiements;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaiementsRepository extends JpaRepository<Paiements, String> {
    List<Paiements> findByStructureSanitaire_Id(String structureId);
    List<Paiements> findByFormules_Id(String formulesId);

}
