package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.JourneeActiviteDTO;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.StatutJournee;
import com.esiitech.monbondocteurv2.repository.JourneeActiviteRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class JourneeActiviteService {

    @Autowired
    private JourneeActiviteRepository repository;
    public JourneeActivite getOrCreate(
            LocalDate date,
            AgendaMedecin agenda
    ) {

        return repository
                .findByDateAndMedecinIdAndStructureSanitaireId(
                        date,
                        agenda.getMedecin().getId(),
                        agenda.getStructureSanitaire().getId()
                )
                .orElseGet(() -> {

                    JourneeActivite j = new JourneeActivite();
                    j.setId("JOUR-" + UUID.randomUUID());
                    j.setDate(date);
                    j.setMedecin(agenda.getMedecin());
                    j.setStructureSanitaire(agenda.getStructureSanitaire());
                    j.setAgenda(agenda);
                    j.setAutorise(true);
                    j.setStatut(StatutJournee.OUVERTE);
                    j.setHeureOuverture(LocalDateTime.now());

                    return repository.save(j);
                });
    }

    public JourneeActiviteDTO toDTO(JourneeActivite j) {
        JourneeActiviteDTO dto = new JourneeActiviteDTO();
        dto.setId(j.getId());
        dto.setDate(j.getDate().toString());
        dto.setAgendaId(j.getAgenda().getId());
        dto.setStatut(j.getStatut().name());


        JourneeActiviteDTO.MedecinDTO med = new JourneeActiviteDTO.MedecinDTO();
        med.setNomMedecin(j.getMedecin().getNomMedecin());
        med.setPrenomMedecin(j.getMedecin().getPrenomMedecin());
        med.setRefSpecialite(j.getMedecin().getRefSpecialite());
        med.setPhotoPath(j.getMedecin().getPhotoPath());
        dto.setMedecin(med);

        JourneeActiviteDTO.StructureSanitaireDTO struct = new JourneeActiviteDTO.StructureSanitaireDTO();
        struct.setNomStructureSanitaire(j.getStructureSanitaire().getNomStructureSanitaire());
        struct.setVille(j.getStructureSanitaire().getVille());
        struct.setPhotoPath(j.getStructureSanitaire().getPhotoPath());
        dto.setStructureSanitaire(struct);

        return dto;
    }
    @Transactional
    public JourneeActivite fermerJournee(String journeeId) {
        JourneeActivite j = repository.findById(journeeId)
                .orElseThrow(() -> new RuntimeException("Journée introuvable"));

        j.setAutorise(false);
        j.setStatut(StatutJournee.FERMEE);
        return repository.save(j);
    }

    public List<JourneeActiviteDTO> getToutesLesJournees() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }
    public JourneeActivite getJourneeById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Journée d'activité introuvable"));
    }

    public List<JourneeActiviteDTO> getJourneesByMedecin(String medecinId) {
        return repository.findByMedecin_IdOrderByDateDesc(medecinId)
                .stream()
                .map(this::toDTO)
                .toList();
    }



}
