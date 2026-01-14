package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.exception.CreneauCompletException;
import com.esiitech.monbondocteurv2.mapper.RendezVousMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final AgendaMedecinRepository agendaMedecinRepository;
    private final StructureSanitaireRepository structureSanitaireRepository;
    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final JourneeActiviteService journeeActiviteService;
    private final RendezVousMapper rendezVousMapper;
    private final NotificationService notificationService;

    public RendezVousService(
            RendezVousRepository rendezVousRepository,
            AgendaMedecinRepository agendaMedecinRepository,
            StructureSanitaireRepository structureSanitaireRepository,
            MedecinRepository medecinRepository,
            UtilisateurRepository utilisateurRepository,
            JourneeActiviteService journeeActiviteService,
            RendezVousMapper rendezVousMapper,
            NotificationService notificationService
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.agendaMedecinRepository = agendaMedecinRepository;
        this.structureSanitaireRepository = structureSanitaireRepository;
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.journeeActiviteService = journeeActiviteService;
        this.rendezVousMapper = rendezVousMapper;
        this.notificationService = notificationService;
    }

    /* ============================================================
       CRÉATION RENDEZ-VOUS (POINT CENTRAL DU SYSTÈME)
       ============================================================ */
    @Transactional
    public RendezVousDTO creerRendezVous(RendezVousDTO dto) {

        /* 1️⃣ Agenda */
        AgendaMedecin agenda = agendaMedecinRepository.findById(dto.getAgendaId())
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        if (!agenda.isAutorise()) {
            throw new RuntimeException("Agenda désactivé");
        }

        /* 2️⃣ Date */
        LocalDate date = dto.getDate();
        if (date == null) {
            throw new RuntimeException("La date est obligatoire");
        }

        /* 3️⃣ Journée d’activité */
        JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

        if (!journee.isAutorise()) {
            throw new RuntimeException("La journée est fermée");
        }

        /* 4️⃣ Limite patient (2 RDV / jour) */
        int rdvPatient = rendezVousRepository
                .countByJourneeActivite_IdAndEmail(journee.getId(), dto.getEmail());

        if (rdvPatient >= 2) {
            throw new RuntimeException("Limite de 2 rendez-vous atteinte pour cette journée");
        }

        /* 5️⃣ Capacité journée */
        int capacite = agenda.getPlages().stream()
                .filter(PlageHoraire::isAutorise)
                .mapToInt(p -> p.getNombrePatients() != null ? p.getNombrePatients() : 0)
                .sum();

        int rdvJournee = rendezVousRepository
                .countByJourneeActivite_Id(journee.getId());

        if (rdvJournee >= capacite) {
            throw new RuntimeException("Plus aucun créneau disponible");
        }
        /* 6️⃣ Récupération de la plage horaire */
        PlageHoraire plageSelectionnee = agenda.getPlages().stream()
                .filter(PlageHoraire::isAutorise)
                .filter(p -> p.getHeureDebut().equals(dto.getHeureDebut()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Plage horaire introuvable"));

        /* Vérification capacité restante */
        if (plageSelectionnee.getNombrePatientsRestants() <= 0) {
            throw new CreneauCompletException("Ce créneau est complet");
        }



        /* 7️⃣ Détermination période (MATIN / SOIR) */
        PeriodeJournee periode = determinerPeriode(dto.getHeureDebut());

        /* 7️⃣ Création RDV */
        RendezVous rdv = new RendezVous();
        rdv.setId(generateId());

        rdv.setNom(dto.getNom());
        rdv.setPrenom(dto.getPrenom());
        rdv.setEmail(dto.getEmail());
        rdv.setSexe(dto.getSexe());
        rdv.setAge(dto.getAge());
        rdv.setAdresse(dto.getAdresse());
        rdv.setTelephone(dto.getTelephone());
        rdv.setMotif(dto.getMotif());

        rdv.setDate(date);
        rdv.setHeureDebut(dto.getHeureDebut());
        rdv.setPeriodeJournee(periode);
        rdv.setPlageHoraire(plageSelectionnee);

        rdv.setAgendaMedecin(agenda);
        rdv.setJourneeActivite(journee);
        rdv.setMedecin(agenda.getMedecin());
        rdv.setStructureSanitaire(agenda.getStructureSanitaire());

        /* 8️⃣ Spécialités */
        Set<String> specialites = new HashSet<>();
        String sp = agenda.getMedecin().getRefSpecialite();
        if (sp != null && !sp.isBlank()) {
            specialites.add(sp.trim());
        }
        rdv.setRefSpecialites(specialites);

        /* 9️⃣ Utilisateur connecté */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            utilisateurRepository.findByEmail(auth.getName())
                    .ifPresent(rdv::setUtilisateur);
        }

        /* 🔟 Sauvegarde */
        RendezVous saved = rendezVousRepository.save(rdv);

        /* 🔔 Notifications */
        notificationService.envoyerAuPatient(
                saved.getEmail(),
                saved.getNom(),
                agenda.getMedecin().getNomMedecin()
        );

        notificationService.envoyerAuMedecin(
                agenda.getMedecin().getEmail(),
                agenda.getMedecin().getNomMedecin(),
                saved.getNom()
        );

        return rendezVousMapper.toDTO(saved);
    }

    /* ============================================================
       MÉTHODES UTILITAIRES
       ============================================================ */
    private String generateId() {
        return "RDV-" + UUID.randomUUID();
    }

    public List<RendezVousDTO> listerTous() {
        return rendezVousRepository.findAll()
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public Optional<RendezVousDTO> trouverParId(String id) {
        return rendezVousRepository.findById(id)
                .map(rendezVousMapper::toDTO);
    }

    private PeriodeJournee determinerPeriode(LocalTime heureDebut) {
        return heureDebut.isBefore(LocalTime.NOON)
                ? PeriodeJournee.MATIN
                : PeriodeJournee.SOIR;
    }

    public List<RendezVousDTO> trouverParMedecin(Medecin medecin) {
        return rendezVousRepository.findByMedecin(medecin)
                .stream().map(rendezVousMapper::toDTO).toList();
    }

    public List<RendezVousDTO> trouverParMedecinId(String medecinId) {
        return rendezVousRepository.findByMedecin_Id(medecinId)
                .stream().map(rendezVousMapper::toDTO).toList();
    }

    public List<RendezVousDTO> trouverParAgendaId(String agendaId) {
        agendaMedecinRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));
        return rendezVousRepository.findByAgendaMedecin_Id(agendaId)
                .stream().map(rendezVousMapper::toDTO).toList();
    }

    public List<RendezVousDTO> trouverParStructure(String nomStructure) {
        StructureSanitaire structure = structureSanitaireRepository
                .findByNomStructureSanitaireIgnoreCase(nomStructure)
                .orElseThrow(() -> new RuntimeException("Structure introuvable"));
        return rendezVousRepository.findByStructureSanitaire(structure)
                .stream().map(rendezVousMapper::toDTO).toList();
    }

    public void supprimer(String id) {
        rendezVousRepository.deleteById(id);
    }
    @Transactional
    public RendezVousDTO modifierStatut(String rdvId, boolean actif) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));

        rdv.setActif(actif);

        // Archiver automatiquement si le rendez-vous est désactivé
        if (!actif) {
            rdv.setArchive(true);
        }

        RendezVous updated = rendezVousRepository.save(rdv);
        return rendezVousMapper.toDTO(updated);
    }


    @Transactional
    public List<RendezVousDTO> modifierStatutTousParJournee(String journeeId, boolean actif) {
        List<RendezVous> rdvs = rendezVousRepository.findByJourneeActivite_Id(journeeId);
        rdvs.forEach(rdv -> {
            rdv.setActif(actif);
            if (!actif) rdv.setArchive(true);
        });
        List<RendezVous> updated = rendezVousRepository.saveAll(rdvs);
        return updated.stream().map(rendezVousMapper::toDTO).toList();
    }


    @Transactional
    public List<RendezVousDTO> modifierStatutTousParAgenda(String agendaId, boolean actif) {
        List<RendezVous> rdvs = rendezVousRepository.findByAgendaMedecin_Id(agendaId);
        rdvs.forEach(rdv -> {
            rdv.setActif(actif);
            if (!actif) rdv.setArchive(true);
        });        List<RendezVous> updated = rendezVousRepository.saveAll(rdvs);


        return updated.stream().map(rendezVousMapper::toDTO).toList();
    }

    @Transactional
    public void desactiverTousLesRdvDeLaJournee(String journeeId) {
        List<RendezVous> rdvs = rendezVousRepository.findByJourneeActivite_Id(journeeId);

        rdvs.forEach(rdv -> {
            rdv.setActif(false);
            rdv.setArchive(true);
        });

        rendezVousRepository.saveAll(rdvs);
    }

}
