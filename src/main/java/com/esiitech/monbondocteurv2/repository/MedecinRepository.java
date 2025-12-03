package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Medecin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedecinRepository extends JpaRepository<Medecin, String> {
    Optional<Medecin> findByEmail(String email);

    List<Medecin> findByRefSpecialite(String RefSpecialite);

    List<Medecin> findByActif(boolean actif);

    // Dans MedecinRepository
    Optional<Medecin> findByNomMedecinIgnoreCase(String nomMedecin);


    // Recherche partielle sur nom OU prénom (insensible à la casse)
    List<Medecin> findByNomMedecinIgnoreCaseContainingOrPrenomMedecinIgnoreCaseContaining(String nom, String prenom);

    // Variante pageable si tu veux paginer les résultats
    Page<Medecin> findByNomMedecinIgnoreCaseContainingOrPrenomMedecinIgnoreCaseContaining(String nom, String prenom, Pageable pageable);


}
