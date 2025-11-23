package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.repository.UtilisateurRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;
    private final StructureSanitaireRepository structureSanitaireRepository;

    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository,
                                    MedecinRepository medecinRepository, StructureSanitaireService structureSanitaireService, StructureSanitaireRepository structureSanitaireRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.medecinRepository = medecinRepository;
        this.structureSanitaireRepository = structureSanitaireRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilisateur user = utilisateurRepository.findByEmail(email).orElse(null);
        if (user != null) {
            return new CustomUserDetails(
                    user.getId(),
                    user.getEmail(),
                    user.getMotDePasse(),
                    user.getAuthorities(),
                    user.getNom(),
                    user.getRole().name(),
                    false // ✅ pas d’abonnement pour Utilisateur
            );
        }

        Medecin medecin = medecinRepository.findByEmail(email).orElse(null);
        if (medecin != null) {
            return new CustomUserDetails(
                    medecin.getId(),
                    medecin.getEmail(),
                    medecin.getMotDePasse(),
                    medecin.getAuthorities(),
                    medecin.getNomMedecin(),
                    medecin.getRole().name(),
                    false // ✅ pas d’abonnement pour Utilisateur
            );
        }

        StructureSanitaire structureSanitaire = structureSanitaireRepository.findByEmail(email).orElse(null);
        if (structureSanitaire != null) {
            return new CustomUserDetails(
                    structureSanitaire.getId(),
                    structureSanitaire.getEmail(),
                    structureSanitaire.getMotDePasse(),
                    structureSanitaire.getAuthorities(),
                    structureSanitaire.getNomStructureSanitaire(),
                    structureSanitaire.getRole().name(),
                    structureSanitaire.isAbonneExpire() // ✅

            );

        }
        System.out.println("Connexion en tant que structure sanitaire : " + structureSanitaire.getEmail());
        throw new UsernameNotFoundException("Aucun compte trouvé pour : " + email);
    }

}
