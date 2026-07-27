package com.prestamosfacil.domain.model;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ResultadoEvaluacionManual {

    private UUID solicitudId;
    private EstadoSolicitud estado;
    private UUID prestamoId;
    private String mensaje;
}
