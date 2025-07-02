package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.AgendaMedecinDto;
import com.esiitech.monbondocteurv2.mapper.AgendaMedecinMapper;
import com.esiitech.monbondocteurv2.model.AgendaMedecin;
import com.esiitech.monbondocteurv2.model.Medecin;
import com.esiitech.monbondocteurv2.model.RefSpecialite;
import com.esiitech.monbondocteurv2.repository.AgendaMedecinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgendaMedecinService {

    @Autowired private AgendaMedecinRepository repository;
    @Autowired private AgendaMedecinMapper mapper;
    @Autowired private MedecinStructureSanitaireService medecinStructureSanitaireService;

    public List<AgendaMedecinDto> findAll() {
        return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public Optional<AgendaMedecinDto> findById(Long id) {
        return repository.findById(id).map(mapper::toDto);
    }

    public AgendaMedecinDto save(AgendaMedecinDto dto) {
        AgendaMedecin entity = mapper.toEntity(dto);
        return mapper.toDto(repository.save(entity));
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<AgendaMedecinDto> findByMedecinAndStructure(Long medecinId, Long structureId) {
        List<AgendaMedecin> agendas = repository.findByMedecinAndStructure(medecinId, structureId);
        return agendas.stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public List<AgendaMedecinDto> getAgendasByStructureAndSpecialite(Long structureId, RefSpecialite specialite) {
        List<Medecin> medecins = medecinStructureSanitaireService.getMedecinsByStructureAndSpecialite(structureId, specialite);

        return medecins.stream()
                .flatMap(medecin -> repository.findByMedecinId(medecin.getId()).stream())
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
    public boolean desactiverAgenda(Long agendaId) {
        Optional<AgendaMedecin> optionalAgenda = repository.findById(agendaId);
        if (optionalAgenda.isPresent()) {
            AgendaMedecin agenda = optionalAgenda.get();
            agenda.setActif(false);
            repository.save(agenda);
            return true;
        }
        return false;
    }

}
