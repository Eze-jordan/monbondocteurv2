package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.Avis;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvisRepository extends JpaRepository<Avis, String> {

    // ==================== MÉTHODES EXISTANTES ====================

    Optional<Avis> findByRendezVousId(String rendezVousId);
    Optional<Avis> findByRendezVousIdAndUtilisateurId(String rendezVousId, String utilisateurId);
    List<Avis> findByMedecinIdAndActifTrueOrderByDateCreationDesc(String medecinId);
    List<Avis> findByStructureSanitaireIdAndActifTrueOrderByDateCreationDesc(String structureId);
    List<Avis> findByUtilisateurIdAndActifTrueOrderByDateCreationDesc(String utilisateurId);

    @Query("SELECT AVG(a.note) FROM Avis a WHERE a.medecin.id = :medecinId AND a.actif = true")
    Double getNoteMoyenneByMedecinId(@Param("medecinId") String medecinId);

    @Query("SELECT COUNT(a) FROM Avis a WHERE a.medecin.id = :medecinId AND a.actif = true")
    Long countByMedecinIdAndActifTrue(@Param("medecinId") String medecinId);

    @Query("SELECT a.note, COUNT(a) FROM Avis a WHERE a.medecin.id = :medecinId AND a.actif = true GROUP BY a.note ORDER BY a.note")
    List<Object[]> getRepartitionNotesByMedecinId(@Param("medecinId") String medecinId);

    @Query("SELECT AVG(a.note) FROM Avis a " +
            "WHERE a.structureSanitaire.id = :structureId " +
            "AND a.medecin.refSpecialite = :specialite " +
            "AND a.actif = true")
    Double getNoteMoyenneByStructureIdAndSpecialite(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite);

    @Query("SELECT AVG(a.note) FROM Avis a " +
            "WHERE a.medecin.refSpecialite = :specialite AND a.actif = true")
    Double getNoteMoyenneBySpecialite(@Param("specialite") String specialite);

    @Query("SELECT a.medecin.id, AVG(a.note) as moyenne, COUNT(a) as nombre " +
            "FROM Avis a " +
            "WHERE a.structureSanitaire.id = :structureId " +
            "AND a.medecin.refSpecialite = :specialite " +
            "AND a.actif = true " +
            "GROUP BY a.medecin.id " +
            "ORDER BY moyenne DESC")
    List<Object[]> findTopMedecinsByStructureAndSpecialite(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite);

    @Query("SELECT a.medecin.refSpecialite, AVG(a.note), COUNT(a) " +
            "FROM Avis a " +
            "WHERE a.structureSanitaire.id = :structureId AND a.actif = true " +
            "AND a.medecin.refSpecialite IS NOT NULL " +
            "GROUP BY a.medecin.refSpecialite " +
            "ORDER BY AVG(a.note) DESC")
    List<Object[]> getSpecialitesClassementByStructureId(@Param("structureId") String structureId);

    @Query("SELECT COUNT(a) > 0 FROM Avis a WHERE a.rendezVous.id = :rdvId AND a.utilisateur.id = :userId")
    boolean existsByRendezVousIdAndUtilisateurId(
            @Param("rdvId") String rdvId,
            @Param("userId") String userId);

    // ==================== NOUVELLES MÉTHODES POUR FILTRAGE PAR STRUCTURE ====================

    @Query("SELECT a FROM Avis a " +
            "WHERE a.medecin.id = :medecinId " +
            "AND a.structureSanitaire.id = :structureId " +
            "AND a.actif = true " +
            "ORDER BY a.dateCreation DESC")
    List<Avis> findByMedecinIdAndStructureSanitaireIdAndActifTrueOrderByDateCreationDesc(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId);

    @Query("SELECT AVG(a.note) FROM Avis a " +
            "WHERE a.medecin.id = :medecinId " +
            "AND a.structureSanitaire.id = :structureId " +
            "AND a.actif = true")
    Double getNoteMoyenneByMedecinIdAndStructureSanitaireId(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId);

    @Query("SELECT COUNT(a) FROM Avis a " +
            "WHERE a.medecin.id = :medecinId " +
            "AND a.structureSanitaire.id = :structureId " +
            "AND a.actif = true")
    Long countByMedecinIdAndStructureSanitaireIdAndActifTrue(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId);

    @Query("SELECT a.note, COUNT(a) FROM Avis a " +
            "WHERE a.medecin.id = :medecinId " +
            "AND a.structureSanitaire.id = :structureId " +
            "AND a.actif = true " +
            "GROUP BY a.note " +
            "ORDER BY a.note")
    List<Object[]> getRepartitionNotesByMedecinIdAndStructureSanitaireId(
            @Param("medecinId") String medecinId,
            @Param("structureId") String structureId);

    @Query("SELECT COUNT(a) FROM Avis a " +
            "WHERE a.structureSanitaire.id = :structureId " +
            "AND a.medecin.refSpecialite = :specialite " +
            "AND a.actif = true")
    Long countByStructureIdAndSpecialite(
            @Param("structureId") String structureId,
            @Param("specialite") String specialite);

    long countByActifTrue();

    // ✅ CORRIGÉ : Ajout de la méthode pour les témoignages
    @Query("SELECT a FROM Avis a WHERE a.actif = true AND a.commentaire IS NOT NULL AND a.commentaire != '' ORDER BY a.dateCreation DESC")
    List<Avis> findTopByOrderByDateCreationDesc(Pageable pageable);

    default List<Avis> findTopByOrderByDateCreationDesc(int limit) {
        return findTopByOrderByDateCreationDesc(PageRequest.of(0, limit));
    }
}