package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AvisDto;
import com.esiitech.monbondocteurv2.dto.StatistiquesAvisDto;
import com.esiitech.monbondocteurv2.dto.TestimonialDto;
import com.esiitech.monbondocteurv2.mapper.AvisMapper;
import com.esiitech.monbondocteurv2.model.Avis;
import com.esiitech.monbondocteurv2.model.RendezVous;
import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Utilisateur;
import com.esiitech.monbondocteurv2.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvisService {

    private final AvisRepository avisRepository;
    private final AvisMapper avisMapper;
    private final RendezVousRepository rendezVousRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MedecinRepository medecinRepository;
    private final StructureSanitaireRepository structureSanitaireRepository;

    /**
     * Créer un avis après un rendez-vous
     */
    @Transactional
    public AvisDto creerAvis(AvisDto dto, String emailUtilisateur) {

        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        RendezVous rdv = rendezVousRepository.findById(dto.getRendezVousId())
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

        if (!rdv.getUtilisateur().getId().equals(utilisateur.getId())) {
            throw new RuntimeException("Ce rendez-vous ne vous appartient pas");
        }

        if (rdv.getDate().isAfter(java.time.LocalDate.now())) {
            throw new RuntimeException("Vous ne pouvez noter qu'après la date du rendez-vous");
        }

        if (avisRepository.existsByRendezVousIdAndUtilisateurId(rdv.getId(), utilisateur.getId())) {
            throw new RuntimeException("Vous avez déjà noté ce rendez-vous");
        }

        Avis avis = Avis.builder()
                .id("AVIS-" + UUID.randomUUID().toString().substring(0, 8))
                .rendezVous(rdv)
                .utilisateur(utilisateur)
                .medecin(rdv.getMedecin())
                .structureSanitaire(rdv.getStructureSanitaire())
                .note(dto.getNote())
                .commentaire(dto.getCommentaire())
                .anonyme(dto.isAnonyme())
                .dateCreation(LocalDateTime.now())
                .build();

        Avis saved = avisRepository.save(avis);
        return avisMapper.toDto(saved);
    }

    /**
     * Modifier un avis existant
     */
    @Transactional
    public AvisDto modifierAvis(String avisId, AvisDto dto, String emailUtilisateur) {

        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));

        if (!avis.getUtilisateur().getEmail().equals(emailUtilisateur)) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres avis");
        }

        avis.setNote(dto.getNote());
        avis.setCommentaire(dto.getCommentaire());
        avis.setAnonyme(dto.isAnonyme());
        avis.setDateModification(LocalDateTime.now());

        return avisMapper.toDto(avisRepository.save(avis));
    }

    /**
     * Récupérer les avis et stats d'un médecin (toutes structures confondues)
     */
    public Map<String, Object> getAvisEtStatsMedecin(String medecinId) {

        List<Avis> avis = avisRepository.findByMedecinIdAndActifTrueOrderByDateCreationDesc(medecinId);
        Double noteMoyenne = avisRepository.getNoteMoyenneByMedecinId(medecinId);
        Long totalAvis = avisRepository.countByMedecinIdAndActifTrue(medecinId);

        // Répartition des notes
        List<Object[]> repartition = avisRepository.getRepartitionNotesByMedecinId(medecinId);
        Map<Integer, Long> repartitionMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) repartitionMap.put(i, 0L);
        repartition.forEach(row -> repartitionMap.put((Integer) row[0], (Long) row[1]));

        Map<String, Object> result = new HashMap<>();
        result.put("avis", avis.stream().map(avisMapper::toDto).collect(Collectors.toList()));
        result.put("noteMoyenne", noteMoyenne != null ? Math.round(noteMoyenne * 10.0) / 10.0 : 0.0);
        result.put("totalAvis", totalAvis);
        result.put("repartitionNotes", repartitionMap);

        return result;
    }

    /**
     * ✅ NOUVEAU : Récupérer les avis et stats d'un médecin pour une structure spécifique
     */
    public Map<String, Object> getAvisEtStatsMedecinParStructure(String medecinId, String structureId) {

        List<Avis> avis;
        Double noteMoyenne;
        Long totalAvis;
        List<Object[]> repartition;

        if (structureId != null && !structureId.isEmpty()) {
            // Filtrer par médecin ET par structure
            avis = avisRepository.findByMedecinIdAndStructureSanitaireIdAndActifTrueOrderByDateCreationDesc(medecinId, structureId);
            noteMoyenne = avisRepository.getNoteMoyenneByMedecinIdAndStructureSanitaireId(medecinId, structureId);
            totalAvis = avisRepository.countByMedecinIdAndStructureSanitaireIdAndActifTrue(medecinId, structureId);
            repartition = avisRepository.getRepartitionNotesByMedecinIdAndStructureSanitaireId(medecinId, structureId);
        } else {
            avis = avisRepository.findByMedecinIdAndActifTrueOrderByDateCreationDesc(medecinId);
            noteMoyenne = avisRepository.getNoteMoyenneByMedecinId(medecinId);
            totalAvis = avisRepository.countByMedecinIdAndActifTrue(medecinId);
            repartition = avisRepository.getRepartitionNotesByMedecinId(medecinId);
        }

        // Répartition des notes
        Map<Integer, Long> repartitionMap = new HashMap<>();
        for (int i = 1; i <= 5; i++) repartitionMap.put(i, 0L);
        if (repartition != null) {
            repartition.forEach(row -> repartitionMap.put((Integer) row[0], (Long) row[1]));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("avis", avis.stream().map(avisMapper::toDto).collect(Collectors.toList()));
        result.put("noteMoyenne", noteMoyenne != null ? Math.round(noteMoyenne * 10.0) / 10.0 : 0.0);
        result.put("totalAvis", totalAvis != null ? totalAvis : 0L);
        result.put("repartitionNotes", repartitionMap);

        return result;
    }

    /**
     * Récupérer les statistiques de notes pour une structure
     */
    public StatistiquesAvisDto getStatistiquesStructure(String structureId) {

        StructureSanitaire structure = structureSanitaireRepository.findById(structureId)
                .orElseThrow(() -> new RuntimeException("Structure non trouvée"));

        StatistiquesAvisDto stats = new StatistiquesAvisDto();
        stats.setStructureId(structureId);
        stats.setNomStructure(structure.getNomStructureSanitaire());

        // Classement des spécialités
        List<Object[]> classement = avisRepository.getSpecialitesClassementByStructureId(structureId);

        if (!classement.isEmpty()) {
            // Meilleure spécialité (première du classement)
            Object[] best = classement.get(0);
            stats.setMeilleureSpecialite((String) best[0]);
            stats.setNoteMeilleureSpecialite(Math.round(((Double) best[1]) * 10.0) / 10.0);

            // Toutes les spécialités avec leurs notes
            Map<String, Double> notesParSpecialite = new LinkedHashMap<>();
            Map<String, Long> nombreAvisParSpecialite = new HashMap<>();

            classement.forEach(row -> {
                String specialite = (String) row[0];
                Double note = Math.round(((Double) row[1]) * 10.0) / 10.0;
                Long nombre = (Long) row[2];

                notesParSpecialite.put(specialite, note);
                nombreAvisParSpecialite.put(specialite, nombre);
            });

            stats.setNotesParSpecialite(notesParSpecialite);
            stats.setNombreAvisParSpecialite(nombreAvisParSpecialite);
        }

        return stats;
    }

    /**
     * Supprimer un avis (soft delete)
     */
    @Transactional
    public void supprimerAvis(String avisId, String emailUtilisateur) {

        Avis avis = avisRepository.findById(avisId)
                .orElseThrow(() -> new RuntimeException("Avis non trouvé"));

        if (!avis.getUtilisateur().getEmail().equals(emailUtilisateur)) {
            throw new RuntimeException("Vous ne pouvez supprimer que vos propres avis");
        }

        avis.setActif(false);
        avisRepository.save(avis);
    }

    /**
     * Vérifier si un utilisateur peut noter un rendez-vous
     */
    public boolean peutNoter(String rendezVousId, String emailUtilisateur) {

        RendezVous rdv = rendezVousRepository.findById(rendezVousId)
                .orElseThrow(() -> new RuntimeException("Rendez-vous non trouvé"));

        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (rdv.getDate().isAfter(java.time.LocalDate.now())) {
            return false;
        }

        return !avisRepository.existsByRendezVousIdAndUtilisateurId(rdv.getId(), utilisateur.getId());
    }

    /**
     * Récupérer la note moyenne pour une spécialité dans une structure
     */
    public Double getNoteMoyenneSpecialite(String structureId, String specialite) {
        Double note = avisRepository.getNoteMoyenneByStructureIdAndSpecialite(structureId, specialite);
        return note != null ? Math.round(note * 10.0) / 10.0 : 0.0;
    }

    public Long countAvisByStructureAndSpecialite(String structureId, String specialite) {
        return avisRepository.countByStructureIdAndSpecialite(structureId, specialite);
    }

    public Double getNoteMoyenneByMedecinAndStructure(String medecinId, String structureId) {
        return avisRepository.getNoteMoyenneByMedecinIdAndStructureSanitaireId(medecinId, structureId);
    }

    public Long countAvisByMedecinAndStructure(String medecinId, String structureId) {
        return avisRepository.countByMedecinIdAndStructureSanitaireIdAndActifTrue(medecinId, structureId);
    }

    public List<TestimonialDto> getRecentTestimonials(int limit) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Récupérer les avis les plus récents avec commentaire
        List<Avis> recentAvis = avisRepository.findTopByOrderByDateCreationDesc(limit);

        return recentAvis.stream()
                .filter(avis -> avis.getCommentaire() != null && !avis.getCommentaire().isBlank())
                .map(avis -> {
                    Utilisateur user = avis.getUtilisateur();
                    String nomAnonymise = user.getPrenom() + " " + user.getNom().charAt(0) + ".";

                    return TestimonialDto.builder()
                            .id(Long.valueOf(avis.getId().hashCode()))
                            .nom(nomAnonymise)
                            .prenom(user.getPrenom())
                            .avatar(user.getPhotoPath())
                            .role("Patient")
                            .rating(avis.getNote())
                            .content(avis.getCommentaire())
                            .date(avis.getDateCreation().format(formatter))
                            .consultationType(avis.getRendezVous() != null ? "Consultation confirmée" : "Téléconsultation")
                            .build();
                })
                .collect(Collectors.toList());
    }
}