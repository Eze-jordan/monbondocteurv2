package com.esiitech.monbondocteurv2.controller;

import com.esiitech.monbondocteurv2.dto.HoraireRdvDto;
import com.esiitech.monbondocteurv2.service.HoraireRdvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horaire-rdv")
public class HoraireRdvController {

    @Autowired
    private HoraireRdvService service;

    @GetMapping
    public ResponseEntity<List<HoraireRdvDto>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoraireRdvDto> getById(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HoraireRdvDto> create(@RequestBody HoraireRdvDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HoraireRdvDto> update(@PathVariable Long id, @RequestBody HoraireRdvDto dto) {
        dto.setId(id);
        return ResponseEntity.ok(service.save(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
