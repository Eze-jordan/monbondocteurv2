package com.esiitech.monbondocteurv2.mapper;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.RendezVous;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import org.springframework.stereotype.Component;

@Component
public class RendezVousMapper {

    public RendezVousDTO toDTO(RendezVous rdv) {
        if (rdv == null) return null;

        RendezVousDTO dto = new RendezVousDTO();
        dto.setId(rdv.getId());
        dto.setNom(rdv.getNom());
        dto.setPrenom(rdv.getPrenom());
        dto.setEmail(rdv.getEmail());
        dto.setAdresse(rdv.getAdresse());
        dto.setTelephone(rdv.getTelephone());
        dto.setSexe(rdv.getSexe());
        dto.setAge(rdv.getAge());
        dto.setMotif(rdv.getMotif());
        dto.setDate(rdv.getDate());
        dto.setHeureDebut(rdv.getHeureDebut());
        dto.setStatut(rdv.getStatut());
        dto.setActif(rdv.isActif());
        dto.setPeriodeJournee(rdv.getPeriodeJournee() != null ? rdv.getPeriodeJournee().name() : null);
        dto.setRefSpecialites(rdv.getRefSpecialites());
        dto.setAgendaId(rdv.getAgendaMedecin() != null ? rdv.getAgendaMedecin().getId() : null);
        dto.setStructureId(rdv.getStructureSanitaire() != null ? rdv.getStructureSanitaire().getId() : null);

        // ✅ Infos médecin
        if (rdv.getMedecin() != null) {
            Medecin m = rdv.getMedecin();
            dto.setMedecinId(m.getId());
            dto.setMedecinNom(m.getNomMedecin());
            dto.setMedecinPrenom(m.getPrenomMedecin());
            dto.setMedecinSpecialite(m.getRefSpecialite());
            dto.setMedecinPhoto(m.getPhotoPath());
            dto.setMedecinEmail(m.getEmail());
            dto.setMedecinTelephone(m.getNumeroTelephone());
        }

        // ✅ Infos structure
        if (rdv.getStructureSanitaire() != null) {
            StructureSanitaire s = rdv.getStructureSanitaire();
            dto.setStructureNom(s.getNomStructureSanitaire());
            dto.setStructureAdresse(s.getAdresse());
            dto.setStructureVille(s.getVille());
            dto.setStructurePhoto(s.getPhotoPath());
            dto.setStructureEmail(s.getEmail());
            dto.setStructureTelephone(s.getNumeroTelephone());
            dto.setStructureLatitude(s.getGpsLatitude() != null ? s.getGpsLatitude().doubleValue() : null);
            dto.setStructureLongitude(s.getGpsLongitude() != null ? s.getGpsLongitude().doubleValue() : null);
        }

        return dto;
    }
}