package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {
    Optional<Utilisateur> findByEmail(String email);

    List<Utilisateur> findByActif(boolean actif);

}
