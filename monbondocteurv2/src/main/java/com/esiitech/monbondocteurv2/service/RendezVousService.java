package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.mapper.RendezVousMapper;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final NotificationService notificationService; // Ajout du service de notification
    private final UtilisateurRepository utilisateurRepository;

    @Autowired
    public RendezVousService(
            RendezVousRepository rendezVousRepository,
            StructureSanitaireRepository structureSanitaireRepository,
            MedecinRepository medecinRepository,
            AgendaMedecinRepository agendaMedecinRepository,
            DateRdvRepository dateRdvRepository,
            HoraireRdvRepository horaireRdvRepository,
            RendezVousMapper rendezVousMapper,
            NotificationService notificationService, UtilisateurRepository utilisateurRepository // Injection du service NotificationService
    ) {
        this.rendezVousRepository = rendezVousRepository;
        this.structureSanitaireRepository = structureSanitaireRepository;
        this.medecinRepository = medecinRepository;
        this.agendaMedecinRepository = agendaMedecinRepository;
        this.dateRdvRepository = dateRdvRepository;
        this.horaireRdvRepository = horaireRdvRepository;
        this.rendezVousMapper = rendezVousMapper;
        this.notificationService = notificationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Transactional
    public RendezVousDTO creerRendezVous(RendezVousDTO dto) {
        RendezVous rendezVous = new RendezVous();

        // Récupérer les entités liées au rendez-vous
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

        // Récupérer l'utilisateur authentifié (si vous utilisez Spring Security)
        String emailUtilisateur = SecurityContextHolder.getContext().getAuthentication().getName();
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Créer le rendez-vous
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

        // Associer l'utilisateur au rendez-vous
        rendezVous.setUtilisateur(utilisateur);  // Assurez-vous que cette méthode existe dans votre modèle

        // Sauvegarder le rendez-vous
        RendezVous saved = rendezVousRepository.save(rendezVous);

        // Envoyer les notifications par e-mail
        notificationService.envoyerAuPatient(dto.getEmail(), dto.getNom(), medecin.getNomMedecin());
        notificationService.envoyerAuMedecin(medecin.getEmail(), medecin.getNomMedecin(), dto.getNom());

        // Retourner le DTO du rendez-vous sauvegardé
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
