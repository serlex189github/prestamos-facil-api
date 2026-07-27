package com.prestamosfacil.infrastructure.adapter.out.notification;

import com.prestamosfacil.application.port.out.NotificacionPrestamoPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class NotificacionPrestamoLogAdapter
    implements NotificacionPrestamoPort {

    @Override
    public void notificarDecision(
        UUID usuarioId,
        UUID solicitudId,
        EstadoSolicitud estado
    ) {
        log.info(
            "Notificación simulada: usuarioId={}, solicitudId={}, estado={}",
            usuarioId,
            solicitudId,
            estado
        );
    }
}
