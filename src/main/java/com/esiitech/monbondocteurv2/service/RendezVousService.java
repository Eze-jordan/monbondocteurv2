package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AttributionRdvRequest;
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

        /* 2️⃣ Date */
        LocalDate date = dto.getDate();
        if (date == null) {
            throw new RuntimeException("La date est obligatoire");
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new RuntimeException("Impossible de prendre un rendez-vous dans le passé");
        }

// optionnel : si RDV aujourd'hui, interdire une heure déjà passée
        if (date.isEqual(today) && dto.getHeureDebut() != null && dto.getHeureDebut().isBefore(LocalTime.now())) {
            throw new RuntimeException("Impossible de prendre un rendez-vous à une heure déjà passée");
        }

        // ✅ remplace agenda par la version effective pour cette date
        agenda = agendaEffectifPourDate(
                agenda.getMedecin().getId(),
                agenda.getStructureSanitaire().getId(),
                date
        );

        if (!agenda.isAutorise()) {
            throw new RuntimeException("Agenda désactivé");
        }

        /* ✅ Heure obligatoire (sinon impossible de choisir une plage) */
        if (dto.getHeureDebut() == null) {
            throw new RuntimeException("L'heure de début est obligatoire");
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
                .countByJourneeActivite_IdAndActifTrueAndArchiveFalse(journee.getId());

        if (rdvJournee >= capacite) {
            throw new RuntimeException("Plus aucun créneau disponible");
        }

        /* 6️⃣ Récupération de la plage horaire (intervalle) */
        PlageHoraire plageSelectionnee = trouverPlagePourHeure(agenda, dto.getHeureDebut());

        /* ✅ Vérification capacité restante PAR JOURNÉE + PLAGE */
        int capPlage = (plageSelectionnee.getNombrePatients() != null) ? plageSelectionnee.getNombrePatients() : 0;

        int used = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndActifTrueAndArchiveFalse(
                journee.getId(),
                plageSelectionnee.getId()
        );

        if (used >= capPlage) {
            throw new CreneauCompletException("Ce créneau est complet");
        }

        /* 7️⃣ Détermination période (MATIN / SOIR) */
        PeriodeJournee periode = determinerPeriode(dto.getHeureDebut());

        /* 8️⃣ Création RDV */
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

        // ✅ IMPORTANT : on persist l'heure
        rdv.setHeureDebut(dto.getHeureDebut());

        rdv.setPeriodeJournee(periode);
        rdv.setPlageHoraire(plageSelectionnee);

        rdv.setAgendaMedecin(agenda);
        rdv.setJourneeActivite(journee);
        rdv.setMedecin(agenda.getMedecin());
        rdv.setStructureSanitaire(agenda.getStructureSanitaire());
        rdv.setStatut(StatutRendezVous.CONFIRME);
        rdv.setActif(true);
        rdv.setArchive(false);

        /* 9️⃣ Spécialités */
        Set<String> specialites = new HashSet<>();
        String sp = agenda.getMedecin().getRefSpecialite();
        if (sp != null && !sp.isBlank()) {
            specialites.add(sp.trim());
        }
        rdv.setRefSpecialites(specialites);

        /* 🔟 Utilisateur connecté */
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            utilisateurRepository.findByEmail(auth.getName())
                    .ifPresent(rdv::setUtilisateur);
        }

        /* Sauvegarde */
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

    /* ✅ AJOUTE ICI */
    private PlageHoraire trouverPlagePourHeure(AgendaMedecin agenda, LocalTime heure) {
        if (agenda.getPlages() == null || agenda.getPlages().isEmpty()) {
            throw new RuntimeException("Aucune plage horaire disponible pour cet agenda");
        }

        return agenda.getPlages().stream()
                .filter(PlageHoraire::isAutorise)
                .filter(p -> p.getHeureDebut() != null && p.getHeureFin() != null)
                .filter(p -> !heure.isBefore(p.getHeureDebut()) && heure.isBefore(p.getHeureFin()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Plage horaire introuvable"));
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
        });
        List<RendezVous> updated = rendezVousRepository.saveAll(rdvs);
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

    /* ============================================================
       DEMANDE RDV (structure/service) EN ATTENTE
       ============================================================ */
    @Transactional
    public RendezVousDTO creerDemandeRdvStructureParService(RendezVousDTO dto) {

        StructureSanitaire structure = structureSanitaireRepository.findById(dto.getStructureId())
                .orElseThrow(() -> new RuntimeException("Structure introuvable"));

        if (dto.getSpecialite() == null || dto.getSpecialite().isBlank()) {
            throw new RuntimeException("Le service (spécialité) est obligatoire");
        }

        boolean ok = structure.getRefSpecialites() != null &&
                structure.getRefSpecialites().stream()
                        .anyMatch(s -> s != null && s.trim().equalsIgnoreCase(dto.getSpecialite().trim()));

        if (!ok) {
            throw new RuntimeException("Ce service n'est pas disponible dans cette structure");
        }

        RendezVous rdv = new RendezVous();
        rdv.setId(generateId());

        rdv.setStructureSanitaire(structure);
        rdv.setStatut(StatutRendezVous.EN_ATTENTE);

        rdv.setDate(dto.getDate());

        rdv.setNom(dto.getNom());
        rdv.setPrenom(dto.getPrenom());
        rdv.setEmail(dto.getEmail());
        rdv.setAdresse(dto.getAdresse());
        rdv.setTelephone(dto.getTelephone());
        rdv.setSexe(dto.getSexe());
        rdv.setAge(dto.getAge());
        rdv.setMotif(dto.getMotif());

        // ✅ stocker le service demandé
        rdv.getRefSpecialites().clear();
        rdv.getRefSpecialites().add(dto.getSpecialite().trim());

        // ✅ champs assignation restent null
        rdv.setAgendaMedecin(null);
        rdv.setMedecin(null);
        rdv.setJourneeActivite(null);
        rdv.setPlageHoraire(null);
        rdv.setHeureDebut(null);
        rdv.setPeriodeJournee(null);

        RendezVous saved = rendezVousRepository.save(rdv);
        return rendezVousMapper.toDTO(saved);
    }

    /* ============================================================
       ATTRIBUTION D’UN RDV EN ATTENTE
       ============================================================ */
    @Transactional
    public RendezVousDTO attribuerRdv(String rdvId, AttributionRdvRequest req) {

        // 1) RDV
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));

        if (rdv.getStatut() != StatutRendezVous.EN_ATTENTE) {
            throw new RuntimeException("Ce rendez-vous n'est pas en attente");
        }

        // 2) Structure connectée
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        StructureSanitaire structureConnectee = structureSanitaireRepository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure connectée introuvable"));

        if (rdv.getStructureSanitaire() == null ||
                !rdv.getStructureSanitaire().getId().equals(structureConnectee.getId())) {
            throw new RuntimeException("Accès refusé : ce RDV n'appartient pas à votre structure");
        }

        // 3) Service demandé
        String serviceDemande = rdv.getRefSpecialites().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Service demandé introuvable"));

        // 4) Médecin
        Medecin medecin = medecinRepository.findById(req.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        boolean medecinOk = medecin.getRefSpecialite() != null
                && medecin.getRefSpecialite().trim().equalsIgnoreCase(serviceDemande.trim());

        if (!medecinOk) {
            throw new RuntimeException("Ce médecin n'appartient pas au service demandé");
        }

        if (medecin.getStructureSanitaire() != null &&
                !medecin.getStructureSanitaire().getId().equals(structureConnectee.getId())) {
            throw new RuntimeException("Ce médecin n'appartient pas à votre structure");
        }

        // 5) Agenda
        AgendaMedecin agenda = agendaMedecinRepository.findById(req.getAgendaId())
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        if (agenda.getStructureSanitaire() != null &&
                !agenda.getStructureSanitaire().getId().equals(structureConnectee.getId())) {
            throw new RuntimeException("Cet agenda n'appartient pas à votre structure");
        }

        if (!agenda.isAutorise()) {
            throw new RuntimeException("Agenda désactivé");
        }

        if (agenda.getMedecin() != null && !agenda.getMedecin().getId().equals(medecin.getId())) {
            throw new RuntimeException("Cet agenda n'appartient pas à ce médecin");
        }

        // 6) Date + heure
        LocalDate date = rdv.getDate();
        if (date == null) {
            throw new RuntimeException("Date RDV manquante");
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new RuntimeException("Impossible d'attribuer un rendez-vous dans le passé");
        }


        // ✅ Agenda effectif pour la date (agenda versionné)
        agenda = agendaEffectifPourDate(
                req.getMedecinId(),
                structureConnectee.getId(),
                date
        );
        // ✅ re-check sur la version effective
        if (!agenda.isAutorise()) {
            throw new RuntimeException("Agenda désactivé pour cette date");
        }

        LocalTime heure = (req.getHeureDebut() != null) ? req.getHeureDebut() : rdv.getHeureDebut();

        if (heure == null) {
            throw new RuntimeException("Heure RDV manquante");
        }

        if (date.isEqual(today) && heure != null && heure.isBefore(LocalTime.now())) {
            throw new RuntimeException("Impossible d'attribuer un rendez-vous à une heure déjà passée");
        }

        // 7) Journée d'activité (avec le bon agenda)
        // récupère la prochaine journée autorisée si celle demandée est fermée
        JourneeActivite journee = getJourneeDisponible(agenda, date);



        // 8) Limite patient (2 RDV / jour)
        int rdvPatient = rendezVousRepository.countByJourneeActivite_IdAndEmail(journee.getId(), rdv.getEmail());
        if (rdvPatient >= 2) {
            throw new RuntimeException("Limite de 2 rendez-vous atteinte pour cette journée");
        }

        // 9) Capacité journée
        int capacite = agenda.getPlages().stream()
                .filter(PlageHoraire::isAutorise)
                .mapToInt(p -> p.getNombrePatients() != null ? p.getNombrePatients() : 0)
                .sum();

        int rdvJournee = rendezVousRepository
                .countByJourneeActivite_IdAndActifTrueAndArchiveFalse(journee.getId());

        if (rdvJournee >= capacite) {
            throw new RuntimeException("Plus aucun créneau disponible");
        }

        // 10) Plage horaire ( intervalle)
        PlageHoraire plageSelectionnee = trouverPlagePourHeure(agenda, heure);

        // ✅ Check complet PAR JOURNÉE + PLAGE (corrigé)
        int capPlage = (plageSelectionnee.getNombrePatients() != null) ? plageSelectionnee.getNombrePatients() : 0;

        int used = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndActifTrueAndArchiveFalse(
                journee.getId(),
                plageSelectionnee.getId()
        );

        if (used >= capPlage) {
            throw new CreneauCompletException("Ce créneau est complet");
        }

        // 11) Assigner + confirmer
        rdv.setMedecin(medecin);
        rdv.setAgendaMedecin(agenda);
        rdv.setJourneeActivite(journee);
        rdv.setPlageHoraire(plageSelectionnee);

        rdv.setHeureDebut(heure);
        rdv.setPeriodeJournee(determinerPeriode(heure));

        rdv.setStatut(StatutRendezVous.CONFIRME);
        rdv.setActif(true);
        rdv.setArchive(false);

        RendezVous saved = rendezVousRepository.save(rdv);

        // 12) Notifications
        notificationService.envoyerAuPatient(
                saved.getEmail(),
                saved.getNom(),
                medecin.getNomMedecin()
        );

        notificationService.envoyerAuMedecin(
                medecin.getEmail(),
                medecin.getNomMedecin(),
                saved.getNom()
        );

        return rendezVousMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<RendezVousDTO> listerDemandesEnAttente(String structureId, String specialite) {
        return rendezVousRepository.findEnAttenteByStructureAndService(structureId, specialite)
                .stream().map(rendezVousMapper::toDTO).toList();
    }
    private JourSemaine toJourSemaine(LocalDate date) {
        // Ton enum JourSemaine = MONDAY..SUNDAY (anglais) => mapping direct
        return JourSemaine.valueOf(date.getDayOfWeek().name());
    }

    private AgendaMedecin agendaEffectifPourDate(String medecinId, String structureId, LocalDate date) {
        JourSemaine jour = toJourSemaine(date);

        return agendaMedecinRepository
                .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecinId,
                        structureId,
                        jour,
                        date
                )
                .orElseThrow(() -> new RuntimeException("Aucun agenda effectif trouvé pour cette date"));
    }
    private JourneeActivite getJourneeDisponible(AgendaMedecin agenda, LocalDate date) {
        LocalDate checkDate = date;

        while (true) {
            JourneeActivite journee = journeeActiviteService.getOrCreate(checkDate, agenda);

            if (journee.isAutorise()) {
                return journee;
            }

            // avancer d'un jour
            checkDate = checkDate.plusDays(1);

            // optionnel : limiter à 1 an pour éviter boucle infinie
            if (checkDate.isAfter(date.plusYears(1))) {
                throw new RuntimeException("Aucune journée disponible pour ce RDV dans l'année à venir");
            }
        }
    }

}
