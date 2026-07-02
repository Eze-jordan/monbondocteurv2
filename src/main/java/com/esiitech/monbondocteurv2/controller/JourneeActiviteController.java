package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.JourneeActiviteDTO;
import com.esiitech.monbondocteurv2.enums.JourSemaine;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.service.JourneeActiviteService;
import com.esiitech.monbondocteurv2.service.RendezVousService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/V2/journees")
public class JourneeActiviteController {

    private final JourneeActiviteService journeeActiviteService;
    private final AgendaMedecinRepository agendaMedecinRepository;
    private final RendezVousService rendezVousService;

    public JourneeActiviteController(
            JourneeActiviteService journeeActiviteService,
            AgendaMedecinRepository agendaMedecinRepository,
            RendezVousService rendezVousService
    ) {
        this.journeeActiviteService = journeeActiviteService;
        this.agendaMedecinRepository = agendaMedecinRepository;
        this.rendezVousService = rendezVousService;
    }

    /**
     * Récupère ou crée une journée d'activité via l'ID d'agenda.
     */
    @GetMapping("/agenda/{agendaId}")
    public ResponseEntity<JourneeActiviteDTO> getOrCreateJournee(
            @PathVariable String agendaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AgendaMedecin agenda = agendaMedecinRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

        return ResponseEntity.ok(journeeActiviteService.toDTO(journee));
    }

    /**
     * Ferme une journée et désactive tous les RDV liés.
     */
    @PostMapping("/{journeeId}/fermer")
    public ResponseEntity<String> fermerJournee(@PathVariable String journeeId) {
        journeeActiviteService.fermerJournee(journeeId);
        rendezVousService.desactiverTousLesRdvDeLaJournee(journeeId);

        return ResponseEntity.ok("Journée fermée avec succès. Tous les rendez-vous ont été désactivés.");
    }

    /**
     * Récupère ou crée la journée pour un médecin/date/structure sans connaître l'ID agenda.
     * Version compatible avec les agendas versionnés via effectiveFrom.
     */
    @GetMapping
    public ResponseEntity<JourneeActiviteDTO> getJourneeByMedecinAndDate(
            @RequestParam String medecinId,
            @RequestParam String structureSanitaireId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        JourSemaine jourSemaine = JourSemaine.valueOf(dayOfWeek.name());

        Optional<AgendaMedecin> agendaOpt = agendaMedecinRepository
                .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecinId,
                        structureSanitaireId,
                        jourSemaine,
                        date
                );

        if (agendaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AgendaMedecin agenda = agendaOpt.get();
        JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

        return ResponseEntity.ok(journeeActiviteService.toDTO(journee));
    }

    /**
     * Récupère toutes les journées d'activité.
     */
    @GetMapping("/all")
    public ResponseEntity<List<JourneeActiviteDTO>> getToutesLesJournees() {
        return ResponseEntity.ok(journeeActiviteService.getToutesLesJournees());
    }

    /**
     * Récupère une journée par ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<JourneeActivite> getJournee(@PathVariable String id) {
        return ResponseEntity.ok(journeeActiviteService.getJourneeById(id));
    }

    /**
     * Récupère les journées par médecin.
     */
    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<JourneeActiviteDTO>> getByMedecin(@PathVariable String medecinId) {
        return ResponseEntity.ok(journeeActiviteService.getJourneesByMedecin(medecinId));
    }
}