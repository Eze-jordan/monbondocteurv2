package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.AvisDto;
import com.esiitech.monbondocteurv2.model.Avis;
import com.esiitech.monbondocteurv2.repository.AvisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AvisMapper {

    @Autowired
    private AvisRepository avisRepository;

    public AvisDto toDto(Avis avis) {
        if (avis == null) return null;

        AvisDto dto = AvisDto.builder()
                .id(avis.getId())
                .rendezVousId(avis.getRendezVous() != null ? avis.getRendezVous().getId() : null)
                .utilisateurId(avis.getUtilisateur() != null ? avis.getUtilisateur().getId() : null)
                .medecinId(avis.getMedecin() != null ? avis.getMedecin().getId() : null)
                .structureSanitaireId(avis.getStructureSanitaire() != null ? avis.getStructureSanitaire().getId() : null)
                .note(avis.getNote())
                .commentaire(avis.getCommentaire())
                .anonyme(avis.isAnonyme())
                .dateCreation(avis.getDateCreation())
                .dateModification(avis.getDateModification())
                .build();

        // Infos utilisateur (si pas anonyme)
        if (!avis.isAnonyme() && avis.getUtilisateur() != null) {
            dto.setNomUtilisateur(avis.getUtilisateur().getNom());
            dto.setPrenomUtilisateur(avis.getUtilisateur().getPrenom());
            dto.setPhotoUtilisateur(avis.getUtilisateur().getPhotoPath());
        }

        // Infos médecin
        if (avis.getMedecin() != null) {
            dto.setNomMedecin(avis.getMedecin().getNomMedecin());
            dto.setPrenomMedecin(avis.getMedecin().getPrenomMedecin());

            // Ajouter les stats du médecin
            Double noteMoyenne = avisRepository.getNoteMoyenneByMedecinId(avis.getMedecin().getId());
            Long totalAvis = avisRepository.countByMedecinIdAndActifTrue(avis.getMedecin().getId());

            dto.setNoteMoyenneMedecin(noteMoyenne != null ? Math.round(noteMoyenne * 10.0) / 10.0 : 0.0);
            dto.setNombreTotalAvisMedecin(totalAvis);
        }

        return dto;
    }

    public Avis toEntity(AvisDto dto) {
        if (dto == null) return null;
        return Avis.builder()
                .id(dto.getId())
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .anonyme(dto.isAnonyme())
                .build();
    }
}