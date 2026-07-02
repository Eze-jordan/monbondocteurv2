package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.enums.StatutCompte;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {

    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findByActif(boolean actif);

    List<Utilisateur> findByStatutCompte(StatutCompte statutCompte);

    @Query("SELECT COUNT(u) FROM Utilisateur u " +
            "WHERE u.statutCompte = com.esiitech.monbondocteurv2.enums.StatutCompte.ACTIF")
    long countByActifTrue();
}