package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.enums.EstadoSolicitud;

import java.util.UUID;

public interface NotificacionPrestamoPort {

    void notificarDecision(
        UUID usuarioId,
        UUID solicitudId,
        EstadoSolicitud estado
    );
}
