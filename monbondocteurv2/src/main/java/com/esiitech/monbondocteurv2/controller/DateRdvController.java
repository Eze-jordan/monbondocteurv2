package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.DateRdvDto;
import com.esiitech.monbondocteurv2.service.DateRdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/date-rdv")
public class  DateRdvController {

    @Autowired
    private DateRdvService service;

    @GetMapping
    public ResponseEntity<List<DateRdvDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DateRdvDto> getById(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/medecin/{medecinId}")
    public ResponseEntity<List<DateRdvDto>> getByMedecin(@PathVariable Long medecinId) {
        return ResponseEntity.ok(service.findByMedecinId(medecinId));
    }

    @PostMapping
    public ResponseEntity<DateRdvDto> create(@RequestBody DateRdvDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DateRdvDto> update(@PathVariable Long id, @RequestBody DateRdvDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}