package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.JourneeActiviteDTO;
import com.esiitech.monbondocteurv2.dto.PlageHoraireDto;
import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.mapper.RendezVousMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.PlageHoraire;          // ✅ IMPORT
import com.esiitech.monbondocteurv2.model.StatutJournee;
import com.esiitech.monbondocteurv2.repository.JourneeActiviteRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
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

    @Autowired
    private RendezVousRepository rendezVousRepository;

    @Autowired
    private RendezVousMapper rendezVousMapper;

    public JourneeActivite getOrCreate(LocalDate date, AgendaMedecin agenda) {

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
        dto.setJour(j.getDate().getDayOfWeek().name());
        dto.setJour(j.getDate().getDayOfWeek().name());

        if (j.getAgenda() != null && j.getAgenda().getPlages() != null) {
            List<PlageHoraireDto> plagesDto = j.getAgenda().getPlages().stream()
                    .filter(PlageHoraire::isAutorise)
                    .map(p -> {
                        PlageHoraireDto pd = new PlageHoraireDto();
                        pd.setPeriode(p.getPeriode());
                        pd.setHeureDebut(p.getHeureDebut());
                        pd.setHeureFin(p.getHeureFin());
                        pd.setNombrePatients(p.getNombrePatients());

                        int cap = (p.getNombrePatients() != null) ? p.getNombrePatients() : 0;
                        int used = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndActifTrueAndArchiveFalse(
                                j.getId(),
                                p.getId()
                        );
                        pd.setNombrePatientsRestants(Math.max(0, cap - used));
                        return pd;
                    })
                    .toList();

            dto.setPlages(plagesDto);
        }


        // -------- Medecin --------
        JourneeActiviteDTO.MedecinDTO med = new JourneeActiviteDTO.MedecinDTO();
        med.setNomMedecin(j.getMedecin().getNomMedecin());
        med.setPrenomMedecin(j.getMedecin().getPrenomMedecin());
        med.setRefSpecialite(j.getMedecin().getRefSpecialite());
        med.setPhotoPath(j.getMedecin().getPhotoPath());
        dto.setMedecin(med);

        // -------- Structure --------
        JourneeActiviteDTO.StructureSanitaireDTO struct = new JourneeActiviteDTO.StructureSanitaireDTO();
        struct.setNomStructureSanitaire(j.getStructureSanitaire().getNomStructureSanitaire());
        struct.setVille(j.getStructureSanitaire().getVille());
        struct.setPhotoPath(j.getStructureSanitaire().getPhotoPath());
        dto.setStructureSanitaire(struct);

        // ✅ -------- Plages (depuis l’agenda) --------
        if (j.getAgenda() != null && j.getAgenda().getPlages() != null) {

            List<PlageHoraireDto> plagesDto = j.getAgenda().getPlages().stream()
                    .filter(PlageHoraire::isAutorise)
                    .map(p -> {
                        PlageHoraireDto pd = new PlageHoraireDto();
                        pd.setId(p.getId());
                        pd.setPeriode(p.getPeriode());
                        pd.setHeureDebut(p.getHeureDebut());
                        pd.setHeureFin(p.getHeureFin());
                        pd.setAutorise(p.isAutorise());
                        pd.setNombrePatients(p.getNombrePatients());

                        // ✅ RESTANTS PAR JOURNÉE (corrigé)
                        int cap = (p.getNombrePatients() != null) ? p.getNombrePatients() : 0;
                        int used = rendezVousRepository
                                .countByJourneeActivite_IdAndPlageHoraire_IdAndActifTrueAndArchiveFalse(
                                        j.getId(),
                                        p.getId()
                                );
                        pd.setNombrePatientsRestants(Math.max(0, cap - used));

                        return pd;
                    })
                    .toList();

            dto.setPlages(plagesDto);
        }

        // ✅ -------- RDVs de la journée --------
        List<RendezVousDTO> rdvsDto = rendezVousRepository
                .findByJourneeActivite_IdOrderByHeureDebutAsc(j.getId())
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();

        dto.setRdvs(rdvsDto);

        return dto;
    }

    @Transactional
    public JourneeActivite fermerJournee(String journeeId) {
        JourneeActivite j = repository.findById(journeeId)
                .orElseThrow(() -> new RuntimeException("Journée introuvable"));

        j.setAutorise(false);
        j.setStatut(StatutJournee.FERMEE);

        // ✅ Optionnel mais logique : désactiver aussi tous les RDV de la journée
        // rendezVousRepository.findByJourneeActivite_Id(journeeId).forEach(r -> { r.setActif(false); r.setArchive(true); });
        // rendezVousRepository.saveAll(...);

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
