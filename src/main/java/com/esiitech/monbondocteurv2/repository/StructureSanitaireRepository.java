package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StructureSanitaireRepository extends JpaRepository<StructureSanitaire, String> {

    Optional<StructureSanitaire> findByEmail(String email);

    Optional<StructureSanitaire> findByNumeroTelephone(String numeroTelephone);

    Optional<StructureSanitaire> findByNomStructureSanitaireIgnoreCase(String nomStructureSanitaire);

    @Query("SELECT s FROM StructureSanitaire s " +
            "WHERE LOWER(s.refSpecialites) LIKE LOWER(CONCAT('%', :specialite, '%'))")
    List<StructureSanitaire> findBySpecialiteContainingIgnoreCase(
            @Param("specialite") String specialite
    );

    @Query("SELECT s FROM StructureSanitaire s " +
            "JOIN s.refSpecialites sp " +
            "WHERE LOWER(sp) = LOWER(:specialite)")
    List<StructureSanitaire> findBySpecialite(
            @Param("specialite") String specialite
    );

    @Query("SELECT DISTINCT s FROM StructureSanitaire s " +
            "JOIN s.refSpecialites sp " +
            "WHERE LOWER(sp) LIKE LOWER(CONCAT('%', :fragment, '%'))")
    List<StructureSanitaire> searchBySpecialiteContains(
            @Param("fragment") String fragment
    );

    List<StructureSanitaire> findByVilleIgnoreCase(String ville);

    boolean existsByEmail(String email);

    boolean existsByNumeroTelephone(String numeroTelephone);

    boolean existsByEmailAndIdNot(String email, String id);

    boolean existsByNumeroTelephoneAndIdNot(String numeroTelephone, String id);

    @Query("SELECT DISTINCT s FROM StructureSanitaire s " +
            "JOIN s.refSpecialites sp " +
            "WHERE LOWER(s.ville) = LOWER(:ville) " +
            "AND LOWER(sp) = LOWER(:specialite)")
    List<StructureSanitaire> findByVilleAndSpecialite(
            @Param("ville") String ville,
            @Param("specialite") String specialite
    );

    long countByActifTrue();
}