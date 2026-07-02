package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.enums.JourSemaine;
import com.esiitech.monbondocteurv2.model.*;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
import com.esiitech.monbondocteurv2.repository.StructureSanitaireRepository;
import com.esiitech.monbondocteurv2.service.JourneeActiviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/V2/public/creneaux")
@RequiredArgsConstructor
public class CreneauPublicController {

    private final AgendaMedecinRepository agendaMedecinRepository;
    private final MedecinRepository medecinRepository;
    private final JourneeActiviteService journeeActiviteService;
    private final RendezVousRepository rendezVousRepository;
    private final StructureSanitaireRepository structureSanitaireRepository;

    /**
     *  ENDPOINT PATIENT : Voir les créneaux disponibles d'un médecin à une date
     *
     * GET /api/V2/public/creneaux?medecinId=100001&structureId=500001&date=2025-06-01
     *
     * PUBLIC - Pas besoin d'être connecté
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCreneaux(
            @RequestParam String medecinId,
            @RequestParam(required = false) String structureId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Validation : pas de RDV dans le passé
        if (date.isBefore(LocalDate.now())) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("erreur", "Impossible de réserver une date passée");
            error.put("dateDemandee", date.toString());
            error.put("dateDuJour", LocalDate.now().toString());
            return ResponseEntity.badRequest().body(error);
        }

        // Validation : limiter à 6 mois à l'avance
        if (date.isAfter(LocalDate.now().plusMonths(6))) {
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("erreur", "Les réservations sont limitées à 6 mois à l'avance");
            error.put("dateDemandee", date.toString());
            error.put("dateMaximale", LocalDate.now().plusMonths(6).toString());
            return ResponseEntity.badRequest().body(error);
        }

        Map<String, Object> response = new LinkedHashMap<>();

        // 1. Infos du médecin
        Medecin medecin = medecinRepository.findById(medecinId).orElse(null);
        if (medecin != null) {
            Map<String, Object> medecinInfo = new LinkedHashMap<>();
            medecinInfo.put("id", medecin.getId());
            medecinInfo.put("nom", medecin.getNomMedecin());
            medecinInfo.put("prenom", medecin.getPrenomMedecin());
            medecinInfo.put("specialite", medecin.getRefSpecialite());
            medecinInfo.put("photoPath", medecin.getPhotoPath());

            // Infos de la structure si spécifiée
            if (structureId != null && !structureId.isEmpty()) {
                StructureSanitaire structure = structureSanitaireRepository.findById(structureId).orElse(null);
                if (structure != null) {
                    medecinInfo.put("structure", Map.of(
                            "id", structure.getId(),
                            "nom", structure.getNomStructureSanitaire(),
                            "adresse", structure.getAdresse(),
                            "ville", structure.getVille()
                    ));
                }
            }

            response.put("medecin", medecinInfo);
        }

        response.put("date", date.toString());
        response.put("jour", date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH));

        // 2. Trouver l'agenda du jour (avec ou sans filtre structure)
        JourSemaine jour = JourSemaine.valueOf(date.getDayOfWeek().name());
        AgendaMedecin agenda = null;

        if (structureId != null && !structureId.isEmpty()) {
            // ✅ Filtrer par structure
            agenda = agendaMedecinRepository
                    .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                            medecinId, structureId, jour, date)
                    .orElse(null);
        } else {
            agenda = agendaMedecinRepository
                    .findFirstByMedecin_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                            medecinId, jour, date)
                    .orElse(null);
        }

        // 3. Générer les créneaux
        List<Map<String, Object>> creneaux = new ArrayList<>();
        boolean agendaActif = false;

        if (agenda != null && agenda.isAutorise()) {
            agendaActif = true;
            JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

            for (PlageHoraire plage : agenda.getPlages()) {
                if (!plage.isAutorise()) continue;

                LocalTime debut = plage.getHeureDebut();
                LocalTime fin = plage.getHeureFin();
                int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

                while (debut.isBefore(fin)) {
                    LocalTime creneauFin = debut.plusMinutes(30);

                    int pris = rendezVousRepository
                            .countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                                    journee.getId(), plage.getId(), debut);

                    int placesRestantes = Math.max(0, capacite - pris);

                    creneaux.add(Map.of(
                            "heure", debut.toString().substring(0, 5),
                            "heureFin", creneauFin.toString().substring(0, 5),
                            "disponible", placesRestantes > 0,
                            "placesRestantes", placesRestantes,
                            "periode", plage.getPeriode().name(),
                            "plageId", plage.getId()
                    ));

                    debut = creneauFin;
                }
            }
        }

        response.put("agendaActif", agendaActif);
        response.put("creneaux", creneaux);
        response.put("totalCreneaux", creneaux.size());
        response.put("creneauxDisponibles", creneaux.stream().filter(c -> (boolean) c.get("disponible")).count());

        return ResponseEntity.ok(response);
    }

    /**
     * Créneaux sur une semaine
     * GET /api/V2/public/creneaux/semaine?medecinId=100001&structureId=500001&date=2025-06-01
     */
    @GetMapping("/semaine")
    public ResponseEntity<Map<String, Object>> getCreneauxSemaine(
            @RequestParam String medecinId,
            @RequestParam(required = false) String structureId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // ✅ Validation
        if (date.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Date passée", "dateDemandee", date.toString()));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        List<Map<String, Object>> jours = new ArrayList<>();

        LocalDate debut = date.with(java.time.DayOfWeek.MONDAY);
        Medecin medecin = medecinRepository.findById(medecinId).orElse(null);

        if (medecin != null) {
            response.put("medecin", Map.of(
                    "id", medecin.getId(),
                    "nom", medecin.getNomMedecin(),
                    "prenom", medecin.getPrenomMedecin(),
                    "specialite", medecin.getRefSpecialite()
            ));
        }

        response.put("medecinId", medecinId);
        if (structureId != null && !structureId.isEmpty()) {
            response.put("structureId", structureId);
        }
        response.put("semaine", debut + " - " + debut.plusDays(6));

        int totalCreneauxSemaine = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate jourDate = debut.plusDays(i);
            JourSemaine jour = JourSemaine.valueOf(jourDate.getDayOfWeek().name());

            AgendaMedecin agenda = null;

            // ✅ Filtrer par structure si fournie
            if (structureId != null && !structureId.isEmpty()) {
                agenda = agendaMedecinRepository
                        .findFirstByMedecin_IdAndStructureSanitaire_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                                medecinId, structureId, jour, jourDate)
                        .orElse(null);
            } else {
                agenda = agendaMedecinRepository
                        .findFirstByMedecin_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                                medecinId, jour, jourDate)
                        .orElse(null);
            }

            boolean disponible = agenda != null && agenda.isAutorise();
            int nbCreneaux = 0;

            if (disponible && agenda.getPlages() != null) {
                for (PlageHoraire plage : agenda.getPlages()) {
                    if (plage.isAutorise() && plage.getHeureDebut() != null && plage.getHeureFin() != null) {
                        // Compter le nombre de créneaux de 30 minutes dans la plage
                        long minutes = java.time.Duration.between(plage.getHeureDebut(), plage.getHeureFin()).toMinutes();
                        nbCreneaux += (int) (minutes / 30);
                    }
                }
            }

            totalCreneauxSemaine += nbCreneaux;

            jours.add(Map.of(
                    "date", jourDate.toString(),
                    "jour", jourDate.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH),
                    "disponible", disponible,
                    "nbCreneaux", nbCreneaux
            ));
        }

        response.put("totalCreneauxSemaine", totalCreneauxSemaine);
        response.put("jours", jours);

        return ResponseEntity.ok(response);
    }
}