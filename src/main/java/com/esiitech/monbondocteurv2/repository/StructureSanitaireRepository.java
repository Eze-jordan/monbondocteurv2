package com.esiitech.monbondocteurv2.repository;

import com.esiitech.monbondocteurv2.model.StructureSanitaire;
import com.esiitech.monbondocteurv2.model.Ville;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StructureSanitaireRepository extends JpaRepository<StructureSanitaire, String> {
    Optional<StructureSanitaire> findByEmail(String email);

    // Dans StructureSanitaireRepository
    Optional<StructureSanitaire> findByNomStructureSanitaireIgnoreCase(String nomStructureSanitaire);
    @Query("SELECT s FROM StructureSanitaire s WHERE LOWER(s.refSpecialites) LIKE LOWER(CONCAT('%', :specialite, '%'))")
    List<StructureSanitaire> findBySpecialiteContainingIgnoreCase(@Param("specialite") String specialite);

    // spécialité exacte (insensible à la casse)
    @Query("""
           select s from StructureSanitaire s
           join s.refSpecialites sp
           where lower(sp) = lower(:specialite)
           """)
    List<StructureSanitaire> findBySpecialite(@Param("specialite") String specialite);

    // "contient" (partiel), via LIKE
    @Query("""
           select distinct s from StructureSanitaire s
           join s.refSpecialites sp
           where lower(sp) like lower(concat('%', :fragment, '%'))
           """)
    List<StructureSanitaire> searchBySpecialiteContains(@Param("fragment") String fragment);

    List<StructureSanitaire> findByVilleIgnoreCase(String ville);


    boolean existsByEmailAndIdNot(String email, String id);
    boolean existsByNumeroTelephoneAndIdNot(String numeroTelephone, String id);


}