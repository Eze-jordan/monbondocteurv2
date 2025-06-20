package com.esiitech.monbondocteurv2.service;

import com.esiitech.monbondocteurv2.dto.HoraireRdvDto;
import com.esiitech.monbondocteurv2.mapper.HoraireRdvMapper;
import com.esiitech.monbondocteurv2.repository.HoraireRdvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class HoraireRdvService {

    @Autowired
    private HoraireRdvRepository horaireRdvRepository;
    @Autowired
    private HoraireRdvMapper mapper;

    public List<HoraireRdvDto> findAll() {
        return horaireRdvRepository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
    }

    public Optional<HoraireRdvDto> findById(Long id) {
        return horaireRdvRepository.findById(id).map(mapper::toDto);
    }

    public HoraireRdvDto save(HoraireRdvDto dto) {
        return mapper.toDto(horaireRdvRepository.save(mapper.toEntity(dto)));
    }

    public void deleteById(Long id) {
        horaireRdvRepository.deleteById(id);
    }
}
