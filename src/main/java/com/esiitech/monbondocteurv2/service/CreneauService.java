package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.*;
import com.esiitech.monbondocteurv2.enums.JourSemaine;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.JourneeActivite;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.PlageHoraire;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.repository.RendezVousRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CreneauService {

    private final AgendaMedecinRepository agendaMedecinRepository;
    private final JourneeActiviteService journeeActiviteService;
    private final RendezVousRepository rendezVousRepository;
    private final MedecinRepository medecinRepository;

    /**
     * Retourne les créneaux pour un jour
     */
    public MedecinCreneauxResponse getCreneauxJour(String medecinId, LocalDate date) {
        Medecin medecin = medecinRepository.findById(medecinId).orElse(null);

        MedecinResumeDto medecinResume = null;
        if (medecin != null) {
            medecinResume = MedecinResumeDto.builder()
                    .id(medecin.getId())
                    .nom(medecin.getNomMedecin())
                    .prenom(medecin.getPrenomMedecin())
                    .specialite(medecin.getRefSpecialite())
                    .photoPath(medecin.getPhotoPath())
                    .build();
        }

        List<CreneauDisponibleDto> creneaux = genererCreneaux(medecinId, date);

        return MedecinCreneauxResponse.builder()
                .medecin(medecinResume)
                .date(date.toString())
                .creneaux(creneaux)
                .build();
    }

    /**
     * Retourne la disponibilité sur 7 jours
     */
    public SemaineCreneauxResponse getCreneauxSemaine(String medecinId, LocalDate dateDebut) {
        LocalDate lundi = dateDebut.with(java.time.DayOfWeek.MONDAY);

        List<JourCreneauDto> jours = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = lundi.plusDays(i);

            // ✅ Seulement bloquer les jours passés (pas de limite future)
            List<CreneauDisponibleDto> creneaux = date.isBefore(LocalDate.now())
                    ? Collections.emptyList()
                    : genererCreneaux(medecinId, date);

            long nbDispos = creneaux.stream().filter(CreneauDisponibleDto::isDisponible).count();

            jours.add(JourCreneauDto.builder()
                    .date(date)
                    .jour(date.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, Locale.FRENCH))
                    .disponible(nbDispos > 0)
                    .nbCreneaux((int) nbDispos)
                    .build());
        }

        return SemaineCreneauxResponse.builder()
                .semaine(lundi + " - " + lundi.plusDays(6))
                .jours(jours)
                .build();
    }

    /**
     *  Génère les créneaux pour un médecin à une date donnée
     * - Ne retourne QUE les créneaux disponibles (placesRestantes > 0)
     * - Pas de créneaux dans le passé
     * - Pas de limite dans le futur
     */
    private List<CreneauDisponibleDto> genererCreneaux(String medecinId, LocalDate date) {
        List<CreneauDisponibleDto> result = new ArrayList<>();

        //  Pas de créneaux dans le passé
        if (date.isBefore(LocalDate.now())) return result;

        JourSemaine jour = JourSemaine.valueOf(date.getDayOfWeek().name());

        AgendaMedecin agenda = agendaMedecinRepository
                .findFirstByMedecin_IdAndJourAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        medecinId, jour, date)
                .orElse(null);

        if (agenda == null || !agenda.isAutorise()) return result;

        JourneeActivite journee = journeeActiviteService.getOrCreate(date, agenda);

        for (PlageHoraire plage : agenda.getPlages()) {
            if (!plage.isAutorise()) continue;

            LocalTime debut = plage.getHeureDebut();
            LocalTime fin = plage.getHeureFin();
            int capacite = plage.getNombrePatients() != null ? plage.getNombrePatients() : 0;

            while (debut.isBefore(fin)) {
                LocalTime creneauFin = debut.plusMinutes(30);
                int pris = rendezVousRepository.countByJourneeActivite_IdAndPlageHoraire_IdAndHeureDebut(
                        journee.getId(), plage.getId(), debut);
                int places = Math.max(0, capacite - pris);

                // ✅ N'ajouter QUE si des places sont disponibles
                if (places > 0) {
                    result.add(CreneauDisponibleDto.builder()
                            .heure(debut.format(DateTimeFormatter.ofPattern("HH:mm")))
                            .heureFin(creneauFin.format(DateTimeFormatter.ofPattern("HH:mm")))
                            .disponible(true)
                            .placesRestantes(places)
                            .periode(plage.getPeriode().name())
                            .plageId(plage.getId())
                            .build());
                }

                debut = creneauFin;
            }
        }
        return result;
    }

    /**
     * Pour le contrôleur public - retourne une Map
     */
    public Map<String, Object> getCreneauxJourMap(String medecinId, LocalDate date) {
        MedecinCreneauxResponse response = getCreneauxJour(medecinId, date);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("medecin", response.getMedecin());
        map.put("date", response.getDate());
        map.put("agendaActif", !response.getCreneaux().isEmpty() && response.getCreneaux().stream().anyMatch(CreneauDisponibleDto::isDisponible));
        map.put("creneaux", response.getCreneaux());
        return map;
    }

    /**
     * Pour le contrôleur public - retourne une Map
     */
    public Map<String, Object> getCreneauxSemaineMap(String medecinId, LocalDate date) {
        SemaineCreneauxResponse response = getCreneauxSemaine(medecinId, date);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("medecinId", medecinId);
        map.put("semaine", response.getSemaine());
        map.put("jours", response.getJours());
        return map;
    }
}