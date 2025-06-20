package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.RendezVousDTO;
import com.esiitech.monbondocteurv2.service.RendezVousService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rendezvous")
@CrossOrigin(origins = "*")
public class RendezVousController {

    private final RendezVousService rendezVousService;

    public RendezVousController(RendezVousService rendezVousService) {
        this.rendezVousService = rendezVousService;
    }

    @PostMapping
    public ResponseEntity<RendezVousDTO> creerRendezVous(@RequestBody RendezVousDTO dto) {
        RendezVousDTO nouveauRdv = rendezVousService.creerRendezVous(dto);
        return ResponseEntity.ok(nouveauRdv);
    }

    @GetMapping
    public ResponseEntity<List<RendezVousDTO>> listerTousLesRendezVous() {
        return ResponseEntity.ok(rendezVousService.listerTous());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RendezVousDTO> trouverRendezVousParId(@PathVariable Long id) {
        return rendezVousService.trouverParId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerRendezVous(@PathVariable Long id) {
        rendezVousService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
