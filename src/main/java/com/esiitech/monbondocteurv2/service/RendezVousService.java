package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.mapper.RendezVousMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RendezVousService {

    private final RendezVousRepository rendezVousRepository;
    private final AgendaMedecinRepository agendaMedecinRepository;
    private final RendezVousMapper rendezVousMapper;
    private final NotificationService notificationService; // Ajout du service de notification
    private final UtilisateurRepository utilisateurRepository;

    private final MedecinRepository medecinRepository;
    private  final StructureSanitaireRepository structureSanitaireRepository;

    private final MedecinStructureSanitaireService medecinStructureSanitaireService;

    @Autowired
    public RendezVousService(
            RendezVousRepository rendezVousRepository,
            StructureSanitaireRepository structureSanitaireRepository,
            AgendaMedecinRepository agendaMedecinRepository,
            RendezVousMapper rendezVousMapper,
            NotificationService notificationService, UtilisateurRepository utilisateurRepository, MedecinRepository medecinRepository, MedecinRepository medecinRepository1, StructureSanitaireRepository structureSanitaireRepository1, // Injection du service NotificationService
            MedecinStructureSanitaireService medecinStructureSanitaireService) {
        this.rendezVousRepository = rendezVousRepository;
        this.agendaMedecinRepository = agendaMedecinRepository;

        this.rendezVousMapper = rendezVousMapper;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
        this.medecinRepository = medecinRepository1;
        this.structureSanitaireRepository = structureSanitaireRepository1;
        this.medecinStructureSanitaireService = medecinStructureSanitaireService;
    }
    @Transactional
    public RendezVousDTO creerRendezVous(RendezVousDTO dto) {
        // Récupérer l'agenda
        AgendaMedecin agenda = agendaMedecinRepository.findById(dto.getAgendaId())
                .orElseThrow(() -> new RuntimeException("Agenda non trouvé"));

       // Vérifier si l'utilisateur a déjà 2 rendez-vous
        int nbRdvExistants = rendezVousRepository.countByAgendaMedecinIdAndEmail(dto.getAgendaId(), dto.getEmail());
        if (nbRdvExistants >= 2) {
            throw new RuntimeException("Vous avez déjà atteint la limite de 2 rendez-vous pour cet agenda.");
        }

        // Vérifier s'il reste des rendez-vous disponibles
        if (agenda.getRdvPris() >= agenda.getNombrePatient()) {
            throw new RuntimeException("Aucun créneau disponible");
        }

        // Incrémenter le nombre de RDV pris
        agenda.setRdvPris(agenda.getRdvPris() + 1);
        agendaMedecinRepository.save(agenda);

        // Construire le rendez-vous
        RendezVous rendezVous = new RendezVous();
        rendezVous.setNom(dto.getNom());
        rendezVous.setPrenom(dto.getPrenom());
        rendezVous.setEmail(dto.getEmail());
        rendezVous.setSexe(dto.getSexe());
        rendezVous.setAge(dto.getAge());
        rendezVous.setAdresse(dto.getAdresse());
        rendezVous.setTelephone(dto.getTelephone());
        rendezVous.setMotif(dto.getMotif());
        rendezVous.setAgendaMedecin(agenda);
        rendezVous.setMedecin(agenda.getMedecin());
        Medecin medecin = agenda.getMedecin();
        rendezVous.setStructureSanitaire(medecinStructureSanitaireService.getUneStructureSanitaireActiveByMedecin(medecin));
        Set<String> specialites = new HashSet<>();

// ton modèle Medecin expose visiblement UNE seule spécialité sous forme de String
        String sp = medecin.getRefSpecialite(); // <- String
        if (sp != null && !sp.isBlank()) {
            specialites.add(sp.trim());
        }

        rendezVous.setRefSpecialites(specialites);

        // Lier l'utilisateur connecté si existant
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        utilisateurRepository.findByEmail(emailConnecte).ifPresent(rendezVous::setUtilisateur);
        if (rendezVous.getId() == null) {
            rendezVous.setId(generateUserId());
        }

        RendezVous saved = rendezVousRepository.save(rendezVous);
     //✅ Notifications
     notificationService.envoyerAuPatient(dto.getEmail(), dto.getNom(), medecin.getNomMedecin());
     notificationService.envoyerAuMedecin(medecin.getEmail(), medecin.getNomMedecin(), dto.getNom());

        return rendezVousMapper.toDTO(saved);
    }

    private String generateUserId() {
        return "RendezVous-" + java.util.UUID.randomUUID();
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
    public List<RendezVousDTO> trouverParStructureSanitaire(StructureSanitaire structureSanitaire) {
        return rendezVousRepository.findByStructureSanitaire(structureSanitaire)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public List<RendezVousDTO> trouverParMedecin(Medecin medecin) {
        return rendezVousRepository.findByMedecin(medecin)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

    public List<RendezVousDTO> trouverParNomStructure(String nomStructureSanitaire) {
        StructureSanitaire structure = structureSanitaireRepository
                .findByNomStructureSanitaireIgnoreCase(nomStructureSanitaire)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée : " + nomStructureSanitaire));
        return rendezVousRepository.findByStructureSanitaire(structure)
                .stream().map(rendezVousMapper::toDTO).toList();
    }

    public List<RendezVousDTO> trouverParNomMedecin(String nomMedecin) {
        Medecin medecin = medecinRepository
                .findByNomMedecinIgnoreCase(nomMedecin)
                .orElseThrow(() -> new RuntimeException("Médecin non trouvé : " + nomMedecin));
        return rendezVousRepository.findByMedecin(medecin)
                .stream().map(rendezVousMapper::toDTO).toList();
    }


    public void supprimer(String id) {
        rendezVousRepository.deleteById(id);
    }

    public List<RendezVousDTO> trouverParDate(LocalDate date) {
        return rendezVousRepository.findByAgendaMedecin_Date(date)
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

        // (optionnel) vérifier que l’agenda existe
        agendaMedecinRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda introuvable : " + agendaId));

        return rendezVousRepository.findByAgendaMedecin_Id(agendaId)
                .stream()
                .map(rendezVousMapper::toDTO)
                .toList();
    }

}
