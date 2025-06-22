package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.DemandeMedecinDTO;
import com.esiitech.monbondocteurv2.service.DemandeMedecinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/demande-medecins")
@CrossOrigin(origins = "*")
public class DemandeMedecinController {

    @Autowired
    private DemandeMedecinService service;

    @PostMapping
    public ResponseEntity<DemandeMedecinDTO> create(@RequestBody DemandeMedecinDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<DemandeMedecinDTO>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DemandeMedecinDTO> getById(@PathVariable Long id) {
        return service.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
