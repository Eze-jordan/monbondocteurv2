package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AttributionRdvRequest;
import com.esiitech.monbondocteurv2.dto.PriseRdvRequest;
import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.enums.JourSemaine;
import com.esiitech.monbondocteurv2.enums.PeriodeJournee;
import com.esiitech.monbondocteurv2.enums.Sexe;
import com.esiitech.monbondocteurv2.enums.StatutRendezVous;
import com.esiitech.monbondocteurv2.exception.CreneauCompletException;
import com.esiitech.monbondocteurv2.mapper.RendezVousMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.PlageHoraire;
import com.esiitech.monbondocteurv2.model.RendezVous;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinStructureSanitaireRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.repository.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final AbonnementStructureService abonnementStructureService;
    private final MedecinStructureSanitaireRepository medecinStructureSanitaireRepository;

    public RendezVousService(
            RendezVousRepository rendezVousRepository,
            AgendaMedecinRepository agendaMedecinRepository,
            StructureSanitaireRepository structureSanitaireRepository,
            MedecinRepository medecinRepository,
            UtilisateurRepository utilisateurRepository,
            JourneeActiviteService journeeActiviteService,
            RendezVousMapper rendezVousMapper,
            NotificationService notificationService,
            AbonnementStructureService abonnementStructureService,
            MedecinStructureSanitaireRepository medecinStructureSanitaireRepository
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.agendaMedecinRepository = agendaMedecinRepository;
        this.structureSanitaireRepository = structureSanitaireRepository;
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.journeeActiviteService = journeeActiviteService;
        this.rendezVousMapper = rendezVousMapper;
        this.notificationService = notificationService;
        this.abonnementStructureService = abonnementStructureService;
        this.medecinStructureSanitaireRepository = medecinStructureSanitaireRepository;
    }

    // ============================================================
    // CRÉATION RENDEZ-VOUS INTERNE / ADMIN
    // ============================================================

    @Transactional
    public RendezVousDTO creerRendezVous(RendezVousDTO dto) {
        AgendaMedecin agenda = agendaMedecinRepository.findById(dto.getAgendaId())
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        verifierAccesStructure(agenda.getStructureSanitaire().getId());

        LocalDate date = dto.getDate();

        if (date == null) {
            throw new RuntimeException("La date est obligatoire");
        }

        verifierDateEtHeure(date, dto.getHeureDebut());

        agenda = agendaEffectifPourDate(
                agenda.getMedecin().getId(),
                agenda.getStructureSanitaire().getId(),
                date
        );

        verifierAccesStructure(agenda.getStructureSanitaire().getId());

        if (!agenda.isAutorise()) {
            throw new RuntimeException("Agenda désactivé");
        }

        if (dto.getHeureDebut() == null) {
            throw new RuntimeException("L'heure de début est obligatoire");
        }

        JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

        if (!journee.isAutorise()) {
            throw new RuntimeException("La journée est fermée");
        }

        int rdvPatient = rendezVousRepository.countByJourneeActivite_IdAndEmail(
                journee.getId(),
                dto.getEmail()
        );

        if (rdvPatient >= 2) {
            throw new RuntimeException("Limite de 2 rendez-vous atteinte pour cette journée");
        }

        PlageHoraire plageSelectionnee = trouverPlagePourHeure(agenda, dto.getHeureDebut());
        verifierCapaciteCreneau(journee, plageSelectionnee, dto.getHeureDebut());

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
        rdv.setPeriodeJournee(determinerPeriode(dto.getHeureDebut()));

        rdv.setPlageHoraire(plageSelectionnee);
        rdv.setAgendaMedecin(agenda);
        rdv.setJourneeActivite(journee);
        rdv.setMedecin(agenda.getMedecin());
        rdv.setStructureSanitaire(agenda.getStructureSanitaire());

        rdv.setStatut(StatutRendezVous.CONFIRME);
        rdv.setActif(true);
        rdv.setArchive(false);

        setSpecialiteRdv(rdv, agenda.getMedecin().getRefSpecialite());

        Utilisateur utilisateurConnecte = getUtilisateurConnecteObligatoire();
        rdv.setUtilisateur(utilisateurConnecte);

        RendezVous saved = rendezVousRepository.save(rdv);

        envoyerNotificationsConfirmation(saved, agenda.getMedecin());

        return rendezVousMapper.toDTO(saved);
    }

    // ============================================================
    // PRISE DE RDV PAR PATIENT CONNECTÉ
    // ============================================================

    @Transactional
    public RendezVousDTO prendreRendezVous(PriseRdvRequest req) {
        Utilisateur utilisateurConnecte = getUtilisateurConnecteObligatoire();

        if (req.getDate() == null) {
            throw new RuntimeException("La date est obligatoire");
        }

        if (req.getHeureDebut() == null) {
            throw new RuntimeException("L'heure de début est obligatoire");
        }

        verifierDateEtHeure(req.getDate(), req.getHeureDebut());

        JourSemaine jour = toJourSemaine(req.getDate());

        AgendaMedecin agenda;

        if (req.getStructureId() != null && !req.getStructureId().isBlank()) {
            verifierAccesStructure(req.getStructureId());

            agenda = agendaMedecinRepository
                    .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                            req.getMedecinId(),
                            req.getStructureId(),
                            jour,
                            req.getDate()
                    )
                    .orElseThrow(() -> new RuntimeException(
                            "Ce médecin n'est pas disponible dans cette structure à cette date"
                    ));
        } else {
            agenda = agendaMedecinRepository
                    .findFirstByMedecin_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                            req.getMedecinId(),
                            jour,
                            req.getDate()
                    )
                    .orElseThrow(() -> new RuntimeException(
                            "Ce médecin n'est pas disponible à cette date"
                    ));

            verifierAccesStructure(agenda.getStructureSanitaire().getId());
        }

        if (!agenda.isAutorise()) {
            throw new RuntimeException("Ce médecin n'est pas disponible");
        }

        PlageHoraire plage = trouverPlagePourHeure(agenda, req.getHeureDebut());

        JourneeActivite journee = journeeActiviteService.getOrCreate(req.getDate(), agenda);

        if (!journee.isAutorise()) {
            throw new RuntimeException("La journée est fermée");
        }

        verifierCapaciteCreneau(journee, plage, req.getHeureDebut());

        RendezVous rdv = new RendezVous();
        rdv.setId("RDV-" + UUID.randomUUID().toString().substring(0, 8));

        rdv.setNom(req.getPatientNom());
        rdv.setPrenom(req.getPatientPrenom());

        rdv.setEmail(
                req.getPatientEmail() != null && !req.getPatientEmail().isBlank()
                        ? req.getPatientEmail()
                        : utilisateurConnecte.getEmail()
        );

        rdv.setTelephone(req.getPatientTelephone());
        rdv.setAge(req.getPatientAge());
        rdv.setMotif(req.getMotif());

        rdv.setAdresse(
                req.getPatientAdresse() != null && !req.getPatientAdresse().isBlank()
                        ? req.getPatientAdresse()
                        : "Non renseignée"
        );

        rdv.setSexe(mapperSexe(req.getPatientGenre()));

        rdv.setDate(req.getDate());
        rdv.setHeureDebut(req.getHeureDebut());
        rdv.setPeriodeJournee(determinerPeriode(req.getHeureDebut()));

        rdv.setAgendaMedecin(agenda);
        rdv.setJourneeActivite(journee);
        rdv.setPlageHoraire(plage);
        rdv.setMedecin(agenda.getMedecin());
        rdv.setStructureSanitaire(agenda.getStructureSanitaire());

        rdv.setStatut(StatutRendezVous.CONFIRME);
        rdv.setActif(true);
        rdv.setArchive(false);

        rdv.setUtilisateur(utilisateurConnecte);

        setSpecialiteRdv(rdv, agenda.getMedecin().getRefSpecialite());

        RendezVous saved = rendezVousRepository.save(rdv);

        envoyerNotificationsConfirmation(saved, agenda.getMedecin());

        return rendezVousMapper.toDTO(saved);
    }

    // ============================================================
    // DEMANDE RDV STRUCTURE / SERVICE EN ATTENTE
    // ============================================================

    @Transactional
    public RendezVousDTO creerDemandeRdvStructureParService(RendezVousDTO dto) {
        StructureSanitaire structure = structureSanitaireRepository.findById(dto.getStructureId())
                .orElseThrow(() -> new RuntimeException("Structure introuvable"));

        verifierAccesStructure(structure.getId());

        if (dto.getSpecialite() == null || dto.getSpecialite().isBlank()) {
            throw new RuntimeException("Le service (spécialité) est obligatoire");
        }

        boolean serviceExiste = structure.getRefSpecialites() != null
                && structure.getRefSpecialites()
                .stream()
                .anyMatch(s -> s != null && s.trim().equalsIgnoreCase(dto.getSpecialite().trim()));

        if (!serviceExiste) {
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
        rdv.setAdresse(dto.getAdresse() != null ? dto.getAdresse() : "Non renseignée");
        rdv.setTelephone(dto.getTelephone());
        rdv.setSexe(dto.getSexe());
        rdv.setAge(dto.getAge());
        rdv.setMotif(dto.getMotif());

        setSpecialiteRdv(rdv, dto.getSpecialite());

        rdv.setAgendaMedecin(null);
        rdv.setMedecin(null);
        rdv.setJourneeActivite(null);
        rdv.setPlageHoraire(null);
        rdv.setHeureDebut(null);
        rdv.setPeriodeJournee(null);

        rdv.setActif(true);
        rdv.setArchive(false);

        getUtilisateurConnecteOptionnel().ifPresent(rdv::setUtilisateur);

        RendezVous saved = rendezVousRepository.save(rdv);

        return rendezVousMapper.toDTO(saved);
    }

    // ============================================================
    // ATTRIBUTION D’UN RDV EN ATTENTE
    // ============================================================

    @Transactional
    public RendezVousDTO attribuerRdv(String rdvId, AttributionRdvRequest req) {
        RendezVous rdv = rendezVousRepository.findById(rdvId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous introuvable"));

        if (rdv.getStatut() != StatutRendezVous.EN_ATTENTE) {
            throw new RuntimeException("Ce rendez-vous n'est pas en attente");
        }

        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();

        StructureSanitaire structureConnectee = structureSanitaireRepository.findByEmail(emailConnecte)
                .orElseThrow(() -> new RuntimeException("Structure connectée introuvable"));

        verifierAccesStructure(structureConnectee.getId());

        if (rdv.getStructureSanitaire() == null
                || !rdv.getStructureSanitaire().getId().equals(structureConnectee.getId())) {
            throw new RuntimeException("Ce RDV n'appartient pas à votre structure");
        }

        Medecin medecin = medecinRepository.findById(req.getMedecinId())
                .orElseThrow(() -> new RuntimeException("Médecin introuvable"));

        String serviceDemande = rdv.getRefSpecialites() != null
                ? rdv.getRefSpecialites().stream().findFirst().orElse("")
                : "";

        if (medecin.getRefSpecialite() == null
                || !medecin.getRefSpecialite().equalsIgnoreCase(serviceDemande)) {
            throw new RuntimeException("Ce médecin n'a pas la spécialité demandée : " + serviceDemande);
        }

        LocalDate dateAttribution = req.getDate() != null ? req.getDate() : rdv.getDate();

        if (dateAttribution == null) {
            throw new RuntimeException("La date du rendez-vous est obligatoire");
        }

        LocalTime heureAttribution = req.getHeureDebut() != null ? req.getHeureDebut() : rdv.getHeureDebut();

        if (heureAttribution == null) {
            throw new RuntimeException("L'heure du rendez-vous est obligatoire");
        }

        verifierDateEtHeure(dateAttribution, heureAttribution);

        JourSemaine jour = toJourSemaine(dateAttribution);

        AgendaMedecin agenda = agendaMedecinRepository
                .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecin.getId(),
                        structureConnectee.getId(),
                        jour,
                        dateAttribution
                )
                .orElseThrow(() -> new RuntimeException("Aucun agenda trouvé pour ce médecin à cette date"));

        if (!agenda.isAutorise()) {
            throw new RuntimeException("L'agenda du médecin n'est pas actif pour cette date");
        }

        PlageHoraire plage = trouverPlagePourHeure(agenda, heureAttribution);

        JourneeActivite journee = journeeActiviteService.getOrCreate(dateAttribution, agenda);

        if (!journee.isAutorise()) {
            throw new RuntimeException("La journée est fermée");
        }

        int rdvPatient = rendezVousRepository.countByJourneeActivite_IdAndEmail(
                journee.getId(),
                rdv.getEmail()
        );

        if (rdvPatient >= 2) {
            throw new RuntimeException("Limite de 2 rendez-vous atteinte pour cette journée");
        }

        verifierCapaciteCreneau(journee, plage, heureAttribution);

        rdv.setMedecin(medecin);
        rdv.setAgendaMedecin(agenda);
        rdv.setJourneeActivite(journee);
        rdv.setPlageHoraire(plage);

        rdv.setDate(dateAttribution);
        rdv.setHeureDebut(heureAttribution);
        rdv.setPeriodeJournee(determinerPeriode(heureAttribution));

        rdv.setStatut(StatutRendezVous.CONFIRME);
        rdv.setActif(true);
        rdv.setArchive(false);

        RendezVous saved = rendezVousRepository.save(rdv);

        envoyerNotificationsConfirmation(saved, medecin);

        return rendezVousMapper.toDTO(saved);
    }

    // ============================================================
    // LISTES / RECHERCHE
    // ============================================================

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

    public List<RendezVousDTO> trouverParMedecin(Medecin medecin) {
        return rendezVousRepository.findByMedecin(medecin)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public List<RendezVousDTO> trouverParMedecinId(String medecinId) {
        return rendezVousRepository.findByMedecin_Id(medecinId)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public List<RendezVousDTO> trouverParAgendaId(String agendaId) {
        agendaMedecinRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        return rendezVousRepository.findByAgendaMedecin_Id(agendaId)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public List<RendezVousDTO> trouverParStructure(String nomStructure) {
        StructureSanitaire structure = structureSanitaireRepository
                .findByNomStructureSanitaireIgnoreCase(nomStructure)
                .orElseThrow(() -> new RuntimeException("Structure introuvable"));

        verifierAccesStructure(structure.getId());

        return rendezVousRepository.findByStructureSanitaire(structure)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RendezVousDTO> listerDemandesEnAttente(String structureId, String specialite) {
        verifierAccesStructure(structureId);

        if (specialite == null || specialite.isBlank()) {
            return rendezVousRepository
                    .findByStructureSanitaire_IdAndStatut(structureId, StatutRendezVous.EN_ATTENTE)
                    .stream()
                    .map(rendezVousMapper::toDTO)
                    .toList();
        }

        return rendezVousRepository.findEnAttenteByStructureAndService(structureId, specialite.trim())
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RendezVousDTO> recupererRendezVousParPatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new RuntimeException("Id patient invalide");
        }

        return rendezVousRepository
                .findByUtilisateur_IdOrderByDateDescHeureDebutDesc(patientId)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RendezVousDTO> recupererRendezVousActifsParPatientId(String patientId) {
        if (patientId == null || patientId.isBlank()) {
            throw new RuntimeException("Id patient invalide");
        }

        return rendezVousRepository
                .findByUtilisateur_IdAndActifTrueAndArchiveFalseOrderByDateDescHeureDebutDesc(patientId)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public List<RendezVousDTO> trouverParMedecinIdEtStructureId(String medecinId, String structureId) {
        verifierAccesStructure(structureId);

        List<RendezVous> rdvs = rendezVousRepository.findByMedecinIdAndStructureSanitaireId(
                medecinId,
                structureId
        );

        return rdvs.stream()
                .map(rendezVousMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getMedecinsDisponibles(String structureId, String specialite, LocalDate date) {
        verifierAccesStructure(structureId);

        if (date == null) {
            throw new RuntimeException("La date est obligatoire");
        }

        List<Map<String, Object>> result = new ArrayList<>();

        List<Medecin> medecins = medecinStructureSanitaireRepository
                .findByStructureSanitaireIdAndSpecialite(structureId, specialite);

        JourSemaine jour = toJourSemaine(date);

        for (Medecin medecin : medecins) {
            Map<String, Object> medecinInfo = new LinkedHashMap<>();
            medecinInfo.put("id", medecin.getId());
            medecinInfo.put("nom", medecin.getNomMedecin());
            medecinInfo.put("prenom", medecin.getPrenomMedecin());
            medecinInfo.put("photoPath", medecin.getPhotoPath());
            medecinInfo.put("specialite", medecin.getRefSpecialite());

            AgendaMedecin agenda = agendaMedecinRepository
                    .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                            medecin.getId(),
                            structureId,
                            jour,
                            date
                    )
                    .orElse(null);

            boolean aDesCreneaux = false;
            List<Map<String, Object>> creneaux = new ArrayList<>();

            if (agenda != null && agenda.isAutorise()) {
                JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

                if (journee.isAutorise()) {
                    for (PlageHoraire plage : agenda.getPlages()) {
                        if (!plage.isAutorise()) {
                            continue;
                        }

                        LocalTime debut = plage.getHeureDebut();
                        LocalTime fin = plage.getHeureFin();
                        int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

                        while (debut.isBefore(fin)) {
                            int pris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                                    journee.getId(),
                                    plage.getId(),
                                    debut
                            );

                            if (pris < capacite) {
                                aDesCreneaux = true;

                                creneaux.add(Map.of(
                                        "heure", debut.toString().substring(0, 5),
                                        "disponible", true
                                ));
                            }

                            debut = debut.plusMinutes(30);
                        }
                    }
                }
            }

            medecinInfo.put("aDesCreneaux", aDesCreneaux);
            medecinInfo.put("creneaux", creneaux);

            result.add(medecinInfo);
        }

        result.sort((a, b) -> {
            boolean aCreneaux = Boolean.TRUE.equals(a.get("aDesCreneaux"));
            boolean bCreneaux = Boolean.TRUE.equals(b.get("aDesCreneaux"));
            return Boolean.compare(bCreneaux, aCreneaux);
        });

        return result;
    }

    // ============================================================
    // MODIFICATION / SUPPRESSION
    // ============================================================

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

            if (!actif) {
                rdv.setArchive(true);
            }
        });

        return rendezVousRepository.saveAll(rdvs)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    @Transactional
    public List<RendezVousDTO> modifierStatutTousParAgenda(String agendaId, boolean actif) {
        List<RendezVous> rdvs = rendezVousRepository.findByAgendaMedecin_Id(agendaId);

        rdvs.forEach(rdv -> {
            rdv.setActif(actif);

            if (!actif) {
                rdv.setArchive(true);
            }
        });

        return rendezVousRepository.saveAll(rdvs)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
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

    // ============================================================
    // MÉTHODES PRIVÉES
    // ============================================================

    private String generateId() {
        return "RDV-" + UUID.randomUUID();
    }

    private PeriodeJournee determinerPeriode(LocalTime heureDebut) {
        return heureDebut.isBefore(LocalTime.NOON)
                ? PeriodeJournee.MATIN
                : PeriodeJournee.SOIR;
    }

    private JourSemaine toJourSemaine(LocalDate date) {
        return JourSemaine.valueOf(date.getDayOfWeek().name());
    }

    private void verifierDateEtHeure(LocalDate date, LocalTime heureDebut) {
        LocalDate today = LocalDate.now();

        if (date.isBefore(today)) {
            throw new RuntimeException("Impossible de prendre un rendez-vous dans le passé");
        }

        if (date.isEqual(today) && heureDebut != null && heureDebut.isBefore(LocalTime.now())) {
            throw new RuntimeException("Impossible de prendre un rendez-vous à une heure déjà passée");
        }
    }

    private AgendaMedecin agendaEffectifPourDate(String medecinId, String structureId, LocalDate date) {
        return agendaMedecinRepository
                .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecinId,
                        structureId,
                        toJourSemaine(date),
                        date
                )
                .orElseThrow(() -> new RuntimeException("Aucun agenda effectif trouvé pour cette date"));
    }

    private PlageHoraire trouverPlagePourHeure(AgendaMedecin agenda, LocalTime heure) {
        if (agenda.getPlages() == null || agenda.getPlages().isEmpty()) {
            throw new RuntimeException("Aucune plage horaire disponible pour cet agenda");
        }

        return agenda.getPlages()
                .stream()
                .filter(PlageHoraire::isAutorise)
                .filter(plage -> plage.getHeureDebut() != null && plage.getHeureFin() != null)
                .filter(plage -> !heure.isBefore(plage.getHeureDebut()) && heure.isBefore(plage.getHeureFin()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Plage horaire introuvable"));
    }

    private void verifierCapaciteCreneau(JourneeActivite journee, PlageHoraire plage, LocalTime heureDebut) {
        int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

        if (capacite <= 0) {
            throw new CreneauCompletException("Ce créneau est complet");
        }

        int pris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                journee.getId(),
                plage.getId(),
                heureDebut
        );

        if (pris >= capacite) {
            throw new CreneauCompletException("Ce créneau est complet");
        }
    }

    private Utilisateur getUtilisateurConnecteObligatoire() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Utilisateur non authentifié");
        }

        return utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
    }

    private Optional<Utilisateur> getUtilisateurConnecteOptionnel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }

        return utilisateurRepository.findByEmail(auth.getName());
    }

    private Sexe mapperSexe(String genre) {
        if (genre == null || genre.isBlank()) {
            return null;
        }

        String value = genre.trim();

        if (value.equalsIgnoreCase("Masculin")
                || value.equalsIgnoreCase("M")
                || value.equalsIgnoreCase("HOMME")
                || value.equalsIgnoreCase("H")) {
            return Sexe.HOMME;
        }

        if (value.equalsIgnoreCase("Féminin")
                || value.equalsIgnoreCase("Feminin")
                || value.equalsIgnoreCase("F")
                || value.equalsIgnoreCase("FEMME")) {
            return Sexe.FEMME;
        }

        return null;
    }

    private void setSpecialiteRdv(RendezVous rdv, String specialite) {
        Set<String> specialites = new HashSet<>();

        if (specialite != null && !specialite.isBlank()) {
            specialites.add(specialite.trim());
        }

        rdv.setRefSpecialites(specialites);
    }

    private void envoyerNotificationsConfirmation(RendezVous rdv, Medecin medecin) {
        try {
            String nomPatient = ((rdv.getNom() != null ? rdv.getNom() : "") + " " +
                    (rdv.getPrenom() != null ? rdv.getPrenom() : "")).trim();

            if (nomPatient.isBlank()) {
                nomPatient = rdv.getNom();
            }

            String date = rdv.getDate() != null ? rdv.getDate().toString() : null;
            String heure = rdv.getHeureDebut() != null ? rdv.getHeureDebut().toString() : null;

            notificationService.envoyerAuPatient(
                    rdv.getEmail(),
                    nomPatient,
                    medecin.getNomMedecin(),
                    date,
                    heure,
                    rdv.getMotif()
            );

            notificationService.envoyerAuMedecin(
                    medecin.getEmail(),
                    medecin.getNomMedecin(),
                    nomPatient,
                    date,
                    heure
            );
        } catch (Exception e) {
            System.err.println("Erreur envoi notification RDV: " + e.getMessage());
        }
    }

    private void verifierAccesStructure(String structureId) {
        if (structureId == null || structureId.isBlank()) {
            throw new IllegalArgumentException("structureId est obligatoire pour vérifier l'abonnement.");
        }

        abonnementStructureService.verifierAccesAbonnement(structureId);
    }
}