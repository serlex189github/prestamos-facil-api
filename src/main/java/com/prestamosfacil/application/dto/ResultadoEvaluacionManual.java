package com.prestamosfacil.application.dto;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ResultadoEvaluacionManual {

    private UUID solicitudId;
    private EstadoSolicitud estado;
    private UUID prestamoId;
    private String mensaje;
}
