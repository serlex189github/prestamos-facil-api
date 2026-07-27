package com.prestamosfacil.infrastructure.adapter.in.rest.dto;

import com.prestamosfacil.domain.enums.EstadoSolicitud;

import java.util.UUID;

public record EvaluarSolicitudManualResponse(
    UUID solicitudId,
    EstadoSolicitud estado,
    UUID prestamoId,
    String mensaje
) {
}
