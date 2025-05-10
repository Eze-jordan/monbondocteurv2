package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.mapper.RendezVousMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final StructureSanitaireRepository structureSanitaireRepository;
    private final MedecinRepository medecinRepository;
    private final AgendaMedecinRepository agendaMedecinRepository;
    private final DateRdvRepository dateRdvRepository;
    private final HoraireRdvRepository horaireRdvRepository;
    private final RendezVousMapper rendezVousMapper;

    public RendezVousService(
            RendezVousRepository rendezVousRepository,
            StructureSanitaireRepository structureSanitaireRepository,
            MedecinRepository medecinRepository,
            AgendaMedecinRepository agendaMedecinRepository,
            DateRdvRepository dateRdvRepository,
            HoraireRdvRepository horaireRdvRepository,
            RendezVousMapper rendezVousMapper
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.structureSanitaireRepository = structureSanitaireRepository;
        this.medecinRepository = medecinRepository;
        this.agendaMedecinRepository = agendaMedecinRepository;
        this.dateRdvRepository = dateRdvRepository;
        this.horaireRdvRepository = horaireRdvRepository;
        this.rendezVousMapper = rendezVousMapper;
    }

    @Transactional
    public RendezVousDTO creerRendezVous(RendezVousDTO dto) {
        RendezVous rendezVous = new RendezVous();

        StructureSanitaire structure = structureSanitaireRepository
                .findById(dto.getStructureSanitaireId())
                .orElseThrow(() -> new RuntimeException("Structure sanitaire introuvable"));
        Medecin medecin = medecinRepository
                .findById(dto.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));
        AgendaMedecin agenda = agendaMedecinRepository
                .findById(dto.getAgendaMedecinId())
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));
        DateRdv dateRdv = dateRdvRepository
                .findById(dto.getDateRdvId())
                .orElseThrow(() -> new RuntimeException("Date RDV introuvable"));
        HoraireRdv horaireRdv = horaireRdvRepository
                .findById(dto.getHoraireRdvId())
                .orElseThrow(() -> new RuntimeException("Horaire RDV introuvable"));

        // Vérifie si un rendez-vous existe déjà à cette date/heure
        Optional<RendezVous> existing = rendezVousRepository
                .findByDateRdvAndHoraireRdv(dateRdv, horaireRdv);
        if (existing.isPresent()) {
            throw new RuntimeException("Un rendez-vous existe déjà à cette date et à cet horaire.");
        }

        rendezVous.setStructureSanitaire(structure);
        rendezVous.setMedecin(medecin);
        rendezVous.setAgendaMedecin(agenda);
        rendezVous.setDateRdv(dateRdv);
        rendezVous.setHoraireRdv(horaireRdv);
        rendezVous.setRefSpecialite(dto.getRefSpecialite());
        rendezVous.setNom(dto.getNom());
        rendezVous.setPrenom(dto.getPrenom());
        rendezVous.setEmail(dto.getEmail());
        rendezVous.setSexe(dto.getSexe());
        rendezVous.setAge(dto.getAge());
        rendezVous.setMotif(dto.getMotif());
        rendezVous.setMontantPaye(dto.getMontantPaye());

        RendezVous saved = rendezVousRepository.save(rendezVous);
        return rendezVousMapper.toDTO(saved);
    }

    public List<RendezVousDTO> listerTous() {
        return rendezVousRepository.findAll()
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public Optional<RendezVousDTO> trouverParId(Long id) {
        return rendezVousRepository.findById(id)
                .map(rendezVousMapper::toDTO);
    }

    public void supprimer(Long id) {
        rendezVousRepository.deleteById(id);
    }
}
