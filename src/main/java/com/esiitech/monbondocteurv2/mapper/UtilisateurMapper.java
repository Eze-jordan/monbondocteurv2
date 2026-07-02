package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.enums.Role;
import com.esiitech.monbondocteurv2.enums.StatutCompte;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    /**
     * =====================================================
     * ENTITY -> DTO
     * =====================================================
     */
    public UtilisateurDto toDto(Utilisateur utilisateur) {

        if (utilisateur == null) {
            return null;
        }

        UtilisateurDto dto = new UtilisateurDto();

        dto.setId(utilisateur.getId());
        dto.setNom(utilisateur.getNom());
        dto.setPrenom(utilisateur.getPrenom());
        dto.setEmail(utilisateur.getEmail());
        dto.setSexe(utilisateur.getSexe());
        dto.setPhotoPath(utilisateur.getPhotoPath());
        dto.setRole(utilisateur.getRole());
        dto.setNumeroTelephone(utilisateur.getNumeroTelephone());

        // ✅ Nouveau système
        dto.setStatut(utilisateur.getStatutCompte());

        return dto;
    }

    /**
     * =====================================================
     * DTO -> ENTITY
     * =====================================================
     */
    public Utilisateur toEntity(UtilisateurDto dto) {

        if (dto == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur();

        utilisateur.setId(dto.getId());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNumeroTelephone(dto.getNumeroTelephone());
        // ⚠️ Le mot de passe doit être hashé dans le service
        // avant sauvegarde
        // utilisateur.setMotDePasse(...)

        utilisateur.setSexe(dto.getSexe());
        utilisateur.setPhotoPath(dto.getPhotoPath());

        // ✅ ROLE PAR DÉFAUT
        utilisateur.setRole(
                dto.getRole() != null
                        ? dto.getRole()
                        : Role.USER
        );

        // ✅ STATUT PAR DÉFAUT
        utilisateur.setStatutCompte(
                dto.getStatut() != null
                        ? dto.getStatut()
                        : StatutCompte.INVITE
        );

        return utilisateur;
    }
}