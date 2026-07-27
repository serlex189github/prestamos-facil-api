package com.prestamosfacil.application.dto;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ResultadoEvaluacionAutomatica {

    UUID solicitudId;
    UUID prestamoId;
    EstadoSolicitud estado;
    String mensaje;
}
