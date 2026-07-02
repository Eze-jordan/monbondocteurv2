package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.dto.ChangementMotDePasseDto;
import com.esiitech.monbondocteurv2.dto.LoginRequest;
import com.esiitech.monbondocteurv2.dto.MedecinDto;
import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.dto.StructureSanitaireDto;
import com.esiitech.monbondocteurv2.dto.UtilisateurDto;
import com.esiitech.monbondocteurv2.enums.JourSemaine;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.repository.MedecinRepository;
import com.esiitech.monbondocteurv2.securite.CustomUserDetails;
import com.esiitech.monbondocteurv2.securite.JwtService;
import com.esiitech.monbondocteurv2.service.AgendaMedecinService;
import com.esiitech.monbondocteurv2.service.AvisService;
import com.esiitech.monbondocteurv2.service.MedecinService;
import com.esiitech.monbondocteurv2.service.RendezVousService;
import com.esiitech.monbondocteurv2.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/V2/medecins")
@Tag(name = "Médecins", description = "Endpoints liés à la gestion des médecins")
public class MedecinController {

    private final MedecinService medecinService;
    private final ValidationService validationService;
    private final MedecinRepository medecinRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AvisService avisService;
    private final RendezVousService rendezVousService;
    private final AgendaMedecinService agendaMedecinService;
    private final ObjectMapper objectMapper;

    public MedecinController(
            MedecinService medecinService,
            ValidationService validationService,
            MedecinRepository medecinRepository,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            AvisService avisService,
            RendezVousService rendezVousService,
            AgendaMedecinService agendaMedecinService,
            ObjectMapper objectMapper
    ) {
        this.medecinService = medecinService;
        this.validationService = validationService;
        this.medecinRepository = medecinRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.avisService = avisService;
        this.rendezVousService = rendezVousService;
        this.agendaMedecinService = agendaMedecinService;
        this.objectMapper = objectMapper;
    }

    // ============================================================
    // CRÉATION / ACTIVATION / CONNEXION
    // ============================================================

    @Operation(
            tags = "Médecins",
            summary = "Créer un médecin",
            description = "Enregistre un médecin avec photo optionnelle et données JSON."
    )
    @PostMapping(
            value = "/create",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MedecinDto> saveMedecin(
            @Parameter(description = "Photo du médecin")
            @RequestParam(value = "photo", required = false) MultipartFile photo,

            @Parameter(description = "Données du médecin au format JSON")
            @RequestParam("medecin") String medecinJson
    ) throws IOException {
        MedecinDto dto = objectMapper.readValue(medecinJson, MedecinDto.class);
        MedecinDto savedMedecin = medecinService.save(dto, photo);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedMedecin);
    }

    @Operation(tags = "Médecins", summary = "Activer un médecin via OTP")
    @PostMapping("/activation")
    public ResponseEntity<String> activation(@RequestBody Map<String, String> activation) {
        try {
            medecinService.activation(activation);
            return ResponseEntity.ok("Compte activé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(tags = "Médecins", summary = "Renvoyer un OTP")
    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody UtilisateurDto dto) {
        Medecin medecin = medecinRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        validationService.renvoyerCodeMedecin(medecin);

        return ResponseEntity.ok("Nouveau code envoyé");
    }

    @Operation(tags = "Médecins", summary = "Connexion d'un médecin")
    @PostMapping(
            value = "/connexion",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> connexion(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getMotDePasse()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    // ============================================================
    // CRUD
    // ============================================================

    @Operation(tags = "Médecins", summary = "Mettre à jour un médecin JSON")
    @PutMapping(
            value = "/update/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MedecinDto> updateMedecinJson(
            @PathVariable String id,
            @RequestBody MedecinDto medecinDto
    ) throws IOException {
        MedecinDto updatedMedecin = medecinService.update(id, medecinDto, null);
        return ResponseEntity.ok(updatedMedecin);
    }

    @Operation(tags = "Médecins", summary = "Mettre à jour un médecin multipart")
    @PutMapping(
            value = "/update/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MedecinDto> updateMedecinMultipart(
            @PathVariable String id,
            @RequestParam("medecin") String medecinJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) throws IOException {
        MedecinDto dto = objectMapper.readValue(medecinJson, MedecinDto.class);
        MedecinDto updatedMedecin = medecinService.update(id, dto, photo);

        return ResponseEntity.ok(updatedMedecin);
    }

    @Operation(tags = "Médecins", summary = "Lister tous les médecins")
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MedecinDto>> getAllMedecins() {
        return ResponseEntity.ok(medecinService.findAll());
    }

    @Operation(tags = "Médecins", summary = "Récupérer un médecin par ID")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedecinDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(medecinService.findByIdDto(id));
    }

    @Operation(tags = "Médecins", summary = "Supprimer un médecin par ID")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedecin(@PathVariable String id) {
        medecinService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(tags = "Médecins", summary = "Changer le statut actif")
    @PutMapping(value = "/update-status/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedecinDto> updateStatus(
            @PathVariable String id,
            @RequestParam boolean actif
    ) {
        return ResponseEntity.ok(medecinService.updateStatus(id, actif));
    }

    @Operation(tags = "Médecins", summary = "Compter les médecins")
    @GetMapping(value = "/count", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> countAllMedecins() {
        return ResponseEntity.ok(medecinService.countAll());
    }

    @Operation(tags = "Médecins", summary = "Rechercher par email")
    @GetMapping(value = "/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MedecinDto> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(medecinService.findByEmail(email));
    }

    @Operation(tags = "Médecins", summary = "Supprimer par email")
    @DeleteMapping("/email/{email}")
    public ResponseEntity<Void> deleteByEmail(@PathVariable String email) {
        medecinService.deleteByEmail(email);
        return ResponseEntity.noContent().build();
    }

    @Operation(tags = "Médecins", summary = "Photo du médecin")
    @GetMapping(value = "/{id}/photo", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getPhoto(@PathVariable String id) throws IOException {
        byte[] image = medecinService.getPhoto(id);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(image);
    }

    // ============================================================
    // RECHERCHE
    // ============================================================

    @Operation(tags = "Médecins", summary = "Rechercher par spécialité")
    @GetMapping(value = "/specialite/{specialite}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MedecinDto>> getBySpecialite(@PathVariable String specialite) {
        return ResponseEntity.ok(medecinService.searchBySpeciality(specialite));
    }

    @Operation(tags = "Médecins", summary = "Médecins actifs")
    @GetMapping(value = "/actifs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MedecinDto>> getActiveMedecins() {
        return ResponseEntity.ok(medecinService.getActiveMedecins());
    }

    @Operation(tags = "Médecins", summary = "Rechercher par nom")
    @GetMapping("/search")
    public ResponseEntity<List<MedecinDto>> searchByName(
            @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(medecinService.searchByName(name));
    }

    @Operation(tags = "Médecins", summary = "Suggestions médecins par nom ou prénom")
    @GetMapping("/suggestions")
    public ResponseEntity<List<MedecinDto>> searchByNomOuPrenom(
            @RequestParam(required = false) String q
    ) {
        return ResponseEntity.ok(medecinService.searchByNomOuPrenom(q));
    }

    // ============================================================
    // MOT DE PASSE / PROFIL CONNECTÉ
    // ============================================================

    @Operation(tags = "Médecins", summary = "Modification du mot de passe")
    @PostMapping(
            value = "/medecin/motdepasse/reset",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public ResponseEntity<String> resetMotDePasse(@RequestBody ChangementMotDePasseDto dto) {
        medecinService.updatePasswordByEmail(dto);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès.");
    }

    @GetMapping("/me")
    @Operation(summary = "Mon profil médecin connecté")
    public ResponseEntity<MedecinDto> getMyProfile() {
        String emailConnecte = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(medecinService.findByEmail(emailConnecte));
    }

    @GetMapping("/{id}/structures")
    @Operation(summary = "Structures rattachées au médecin")
    public ResponseEntity<List<StructureSanitaireDto>> getStructuresByMedecin(@PathVariable String id) {
        return ResponseEntity.ok(medecinService.getStructuresByMedecin(id));
    }

    // ============================================================
    // STATISTIQUES RÉELLES DU MÉDECIN
    // ============================================================

    @GetMapping("/{id}/stats")
    @Operation(
            summary = "Statistiques d'un médecin",
            description = "Retourne les statistiques complètes basées sur les données réelles : avis, RDV, agendas."
    )
    public ResponseEntity<Map<String, Object>> getStatsMedecin(
            @PathVariable String id,
            @RequestParam(required = false) String structureId
    ) {
        Map<String, Object> stats = new LinkedHashMap<>();

        medecinService.findByIdDto(id);

        Map<String, Object> avisStats;

        if (structureId != null && !structureId.isBlank()) {
            avisStats = avisService.getAvisEtStatsMedecinParStructure(id, structureId);
        } else {
            avisStats = avisService.getAvisEtStatsMedecin(id);
        }

        stats.putAll(avisStats);

        List<RendezVousDTO> tousLesRdv;

        if (structureId != null && !structureId.isBlank()) {
            tousLesRdv = rendezVousService.trouverParMedecinIdEtStructureId(id, structureId);
        } else {
            tousLesRdv = rendezVousService.trouverParMedecinId(id);
        }

        stats.put("totalRdv", tousLesRdv.size());

        LocalDate now = LocalDate.now();

        long rdvCeMois = tousLesRdv.stream()
                .filter(rdv -> rdv.getDate() != null
                        && rdv.getDate().getYear() == now.getYear()
                        && rdv.getDate().getMonth() == now.getMonth())
                .count();

        stats.put("rdvCeMois", rdvCeMois);

        Map<String, Long> rdvParMois = new LinkedHashMap<>();

        for (int i = 5; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            int year = month.getYear();
            int monthValue = month.getMonthValue();
            String key = year + "-" + String.format("%02d", monthValue);

            long count = tousLesRdv.stream()
                    .filter(rdv -> rdv.getDate() != null
                            && rdv.getDate().getYear() == year
                            && rdv.getDate().getMonthValue() == monthValue)
                    .count();

            rdvParMois.put(key, count);
        }

        stats.put("rdvParMois", rdvParMois);

        Map<String, Long> rdvParJour = new LinkedHashMap<>();
        rdvParJour.put("MONDAY", countByDay(tousLesRdv, DayOfWeek.MONDAY));
        rdvParJour.put("TUESDAY", countByDay(tousLesRdv, DayOfWeek.TUESDAY));
        rdvParJour.put("WEDNESDAY", countByDay(tousLesRdv, DayOfWeek.WEDNESDAY));
        rdvParJour.put("THURSDAY", countByDay(tousLesRdv, DayOfWeek.THURSDAY));
        rdvParJour.put("FRIDAY", countByDay(tousLesRdv, DayOfWeek.FRIDAY));
        rdvParJour.put("SATURDAY", countByDay(tousLesRdv, DayOfWeek.SATURDAY));
        rdvParJour.put("SUNDAY", countByDay(tousLesRdv, DayOfWeek.SUNDAY));

        stats.put("rdvParJour", rdvParJour);

        List<AgendaMedecinDto> agendas;

        if (structureId != null && !structureId.isBlank()) {
            agendas = agendaMedecinService.getAgendasRecents(id, structureId, now);
        } else {
            agendas = agendaMedecinService.getAgendasRecentsByMedecin(id, now);
        }

        LocalDate debutMois = now.withDayOfMonth(1);
        LocalDate finMois = now.withDayOfMonth(now.lengthOfMonth());

        int totalCreneauxMensuels = 0;

        for (AgendaMedecinDto agenda : agendas) {
            if (!agenda.isAutorise() || agenda.getPlages() == null || agenda.getJour() == null) {
                continue;
            }

            int creneauxJour = agenda.getPlages()
                    .stream()
                    .filter(plage -> plage.isAutorise())
                    .mapToInt(plage -> plage.getNombrePatients() != null ? plage.getNombrePatients() : 0)
                    .sum();

            if (creneauxJour == 0) {
                continue;
            }

            int occurrences = 0;

            for (LocalDate date = debutMois; !date.isAfter(finMois); date = date.plusDays(1)) {
                if (date.getDayOfWeek().name().equals(agenda.getJour().name())) {
                    occurrences++;
                }
            }

            totalCreneauxMensuels += creneauxJour * occurrences;
        }

        double tauxOccupation = totalCreneauxMensuels > 0
                ? (double) rdvCeMois / totalCreneauxMensuels * 100
                : 0;

        stats.put("tauxOccupation", Math.round(tauxOccupation * 10.0) / 10.0);

        long rdvHonores = tousLesRdv.stream()
                .filter(rdv -> rdv.getStatut() != null && "CONFIRME".equals(rdv.getStatut().name()))
                .count();

        double tauxPresence = tousLesRdv.size() > 0
                ? (double) rdvHonores / tousLesRdv.size() * 100
                : 100;

        stats.put("tauxPresence", Math.round(tauxPresence * 10.0) / 10.0);

        List<Map<String, String>> prochainsCreneaux = new ArrayList<>();

        for (AgendaMedecinDto agenda : agendas) {
            if (agenda.isAutorise() && agenda.getPlages() != null && agenda.getJour() != null) {
                LocalDate dateCreneau = findNextDateForDay(agenda.getJour(), now);

                for (var plage : agenda.getPlages()) {
                    if (plage.isAutorise() && prochainsCreneaux.size() < 5) {
                        Map<String, String> creneau = new LinkedHashMap<>();
                        creneau.put("date", dateCreneau.toString());
                        creneau.put(
                                "heureDebut",
                                plage.getHeureDebut() != null ? plage.getHeureDebut().toString() : "08:00"
                        );

                        prochainsCreneaux.add(creneau);
                    }
                }
            }

            if (prochainsCreneaux.size() >= 5) {
                break;
            }
        }

        stats.put("prochainsCreneaux", prochainsCreneaux);

        return ResponseEntity.ok(stats);
    }

    private long countByDay(List<RendezVousDTO> rendezVous, DayOfWeek dayOfWeek) {
        return rendezVous.stream()
                .filter(rdv -> rdv.getDate() != null && rdv.getDate().getDayOfWeek() == dayOfWeek)
                .count();
    }

    private LocalDate findNextDateForDay(JourSemaine jour, LocalDate from) {
        DayOfWeek targetDay = DayOfWeek.valueOf(jour.name());
        LocalDate date = from;

        while (date.getDayOfWeek() != targetDay) {
            date = date.plusDays(1);
        }

        return date;
    }
}