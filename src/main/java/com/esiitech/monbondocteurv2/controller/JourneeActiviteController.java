package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.JourneeActiviteDTO;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourSemaine;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.service.JourneeActiviteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
@RestController
@RequestMapping("/api/V2/journees")
public class JourneeActiviteController {

    @Autowired
    private JourneeActiviteService journeeActiviteService;

    @Autowired
    private AgendaMedecinRepository agendaMedecinRepository;


    /**
     * Récupère ou crée une journée d'activité pour un médecin et une date (via ID d'agenda)
     */
    @GetMapping("/agenda/{agendaId}")
    public ResponseEntity<JourneeActiviteDTO> getOrCreateJournee(
            @PathVariable String agendaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AgendaMedecin agenda = agendaMedecinRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda introuvable"));

        JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);
        // Retourner le DTO pour limiter les infos
        return ResponseEntity.ok(journeeActiviteService.toDTO(journee));
    }

    /**
     * Ferme une journée d'activité
     */
    @PostMapping("/{journeeId}/fermer")
    public ResponseEntity<String> fermerJournee(@PathVariable String journeeId) {
        journeeActiviteService.fermerJournee(journeeId);
        return ResponseEntity.ok("Journée fermée avec succès");
    }

    /**
     * Récupérer ou créer la journée d'activité pour un médecin dans une structure
     * sans connaître l'ID de l'agenda.
     */
    @GetMapping
    public ResponseEntity<JourneeActiviteDTO> getJourneeByMedecinAndDate(
            @RequestParam String medecinId,
            @RequestParam String structureSanitaireId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {

        // 1️⃣ Calculer le jour de la semaine correspondant à la date
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        JourSemaine jourSemaine = JourSemaine.valueOf(dayOfWeek.name());

        // 2️⃣ Chercher l'agenda correspondant au jour de la semaine, médecin et structure
        Optional<AgendaMedecin> agendaOpt = agendaMedecinRepository
                .findByMedecin_IdAndStructureSanitaire_IdAndJour(medecinId, structureSanitaireId, jourSemaine);

        if (agendaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AgendaMedecin agenda = agendaOpt.get();

        // 3️⃣ Créer ou récupérer la journée d'activité
        JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

        // Retourner le DTO
        return ResponseEntity.ok(journeeActiviteService.toDTO(journee));
    }
}
