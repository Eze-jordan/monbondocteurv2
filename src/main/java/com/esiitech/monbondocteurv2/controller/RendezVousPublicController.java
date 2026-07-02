package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.PriseRdvRequest;
import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.service.RendezVousService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/V2/public/rendezvous")
@RequiredArgsConstructor
public class RendezVousPublicController {

    private final RendezVousService rendezVousService;

    /**
     *  ENDPOINT PATIENT : Prendre un rendez-vous
     *
     * POST /api/V2/public/rendezvous
     *
     * Body : {
     *   "medecinId": "100001",
     *   "date": "2025-06-01",
     *   "heureDebut": "08:00",
     *   "patientNom": "Martin",
     *   "patientPrenom": "Sophie",
     *   "patientEmail": "sophie@email.com",
     *   "patientTelephone": "0612345678",
     *   "motif": "Consultation",
     *   "lienParente": "MOI"
     * }
     */
    @PostMapping
    public ResponseEntity<RendezVousDTO> prendreRendezVous(@Valid @RequestBody PriseRdvRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(rendezVousService.prendreRendezVous(request));
    }
}