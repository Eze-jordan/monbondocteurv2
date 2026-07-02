package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Medecin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedecinRepository extends JpaRepository<Medecin, String> {

    Optional<Medecin> findByEmail(String email);

    List<Medecin> findByRefSpecialite(String refSpecialite);

    List<Medecin> findByActif(boolean actif);

    Optional<Medecin> findByNomMedecinIgnoreCase(String nomMedecin);

    List<Medecin> findByNomMedecinIgnoreCaseContainingOrPrenomMedecinIgnoreCaseContaining(
            String nom,
            String prenom
    );

    Page<Medecin> findByNomMedecinIgnoreCaseContainingOrPrenomMedecinIgnoreCaseContaining(
            String nom,
            String prenom,
            Pageable pageable
    );

    @Query("SELECT COUNT(DISTINCT m.refSpecialite) FROM Medecin m WHERE m.actif = true AND m.refSpecialite IS NOT NULL")
    long countDistinctSpecialites();

    long countByActifTrue();

    @Query("SELECT m FROM Medecin m WHERE m.actif = true AND " +
            "(" +
            "LOWER(m.nomMedecin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.prenomMedecin) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(m.prenomMedecin, ' ', m.nomMedecin)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(CONCAT(m.nomMedecin, ' ', m.prenomMedecin)) LIKE LOWER(CONCAT('%', :search, '%'))" +
            ")")
    List<Medecin> searchByNomOuPrenom(
            @Param("search") String searchTerm,
            Pageable pageable
    );
}