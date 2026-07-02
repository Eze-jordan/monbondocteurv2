package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.DashboardStatsDto;
import com.esiitech.monbondocteurv2.service.DashboardStatsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/V2/public/stats")
public class StatsController {

    private final DashboardStatsService dashboardStatsService;

    public StatsController(DashboardStatsService dashboardStatsService) {
        this.dashboardStatsService = dashboardStatsService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Statistiques du dashboard", description = "Retourne les compteurs pour le tableau de bord")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(dashboardStatsService.getDashboardStats());
    }
}