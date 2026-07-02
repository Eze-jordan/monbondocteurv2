package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.DashboardStatsDto;
import com.esiitech.monbondocteurv2.repository.*;
import org.springframework.stereotype.Service;

@Service
public class DashboardStatsService {

    private final StructureSanitaireRepository structureSanitaireRepository;
    private final MedecinRepository medecinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RendezVousRepository rendezVousRepository;
    private final AvisRepository avisRepository;

    public DashboardStatsService(
            StructureSanitaireRepository structureSanitaireRepository,
            MedecinRepository medecinRepository,
            UtilisateurRepository utilisateurRepository,
            RendezVousRepository rendezVousRepository,
            AvisRepository avisRepository) {
        this.structureSanitaireRepository = structureSanitaireRepository;
        this.medecinRepository = medecinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.rendezVousRepository = rendezVousRepository;
        this.avisRepository = avisRepository;
    }

    public DashboardStatsDto getDashboardStats() {

        long totalSpecialites = medecinRepository.countDistinctSpecialites();
        long totalMedecins = medecinRepository.countByActifTrue();
        long totalStructures = structureSanitaireRepository.countByActifTrue();
        long totalUtilisateurs = utilisateurRepository.countByActifTrue();
        long totalRendezVous = rendezVousRepository.countByStatutAndArchiveFalse();
        long totalAvis = avisRepository.countByActifTrue();

        return DashboardStatsDto.builder()
                .totalSpecialites(totalSpecialites)
                .totalMedecins(totalMedecins)
                .totalStructures(totalStructures)
                .totalUtilisateurs(totalUtilisateurs)
                .totalRendezVous(totalRendezVous)
                .totalAvis(totalAvis)
                .build();
    }
}