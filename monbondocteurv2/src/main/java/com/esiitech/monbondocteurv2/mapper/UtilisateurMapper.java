package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class UtilisateurMapper {

    /**
     * Convertir l'entité Utilisateur en DTO de réponse
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
        dto.setActif(utilisateur.isActif());

        return dto;
    }

    /**
     * Convertir le DTO de la demande utilisateur en entité Utilisateur
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
        utilisateur.setMotDePasse(dto.getMotDePasse());  // Hachage à effectuer ici avant de stocker dans la base
        utilisateur.setSexe(dto.getSexe());
        utilisateur.setPhotoPath(dto.getPhotoPath());
        utilisateur.setRole(dto.getRole());
        utilisateur.setActif(dto.isActif());

        return utilisateur;
    }
}
