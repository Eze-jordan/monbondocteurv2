package com.esiitech.monbondocteurv2.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttributionRdvRequest {
    private String medecinId;
    private String agendaId;
    private LocalTime heureDebut;
    private LocalDate date;
}