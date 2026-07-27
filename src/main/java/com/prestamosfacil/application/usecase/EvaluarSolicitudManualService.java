package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.dto.ResultadoEvaluacionManual;
import com.prestamosfacil.application.dto.ResultadoFormalizacionPrestamo;
import com.prestamosfacil.application.port.in.EvaluarSolicitudManualUseCase;
import com.prestamosfacil.application.port.out.NotificacionPrestamoPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.DecisionManual;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EvaluarSolicitudManualService
    implements EvaluarSolicitudManualUseCase {

    private final SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    private final TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;
    private final NotificacionPrestamoPort notificacionPrestamoPort;
    private final FormalizarPrestamoService formalizarPrestamoService;

    @Override
    @Transactional
    public ResultadoEvaluacionManual evaluar(
        UUID solicitudId,
        DecisionManual decision,
        String observacion
    ) {
        validarParametros(solicitudId, decision);

        SolicitudPrestamo solicitud = solicitudPrestamoRepositoryPort
            .buscarPorId(solicitudId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe la solicitud de préstamo con id: " + solicitudId
                )
            );

        validarEstadoActual(solicitud);

        if (decision == DecisionManual.RECHAZAR) {
            return rechazar(solicitud, observacion);
        }

        return aprobar(solicitud, observacion);
    }

    private ResultadoEvaluacionManual rechazar(
        SolicitudPrestamo solicitud,
        String observacion
    ) {
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaDecision(Instant.now());
        solicitud.setObservacionDecision(observacion);

        SolicitudPrestamo solicitudGuardada =
            solicitudPrestamoRepositoryPort.guardar(solicitud);

        notificacionPrestamoPort.notificarDecision(
            solicitudGuardada.getUsuarioId(),
            solicitudGuardada.getId(),
            solicitudGuardada.getEstado()
        );

        return ResultadoEvaluacionManual.builder()
            .solicitudId(solicitudGuardada.getId())
            .estado(solicitudGuardada.getEstado())
            .prestamoId(null)
            .mensaje("Solicitud rechazada correctamente")
            .build();
    }

    private ResultadoEvaluacionManual aprobar(
        SolicitudPrestamo solicitud,
        String observacion
    ) {
        TipoPrestamo tipoPrestamo = tipoPrestamoRepositoryPort
            .buscarPorId(solicitud.getTipoPrestamoId())
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el tipo de préstamo con id: "
                        + solicitud.getTipoPrestamoId()
                )
            );

        ResultadoFormalizacionPrestamo resultado =
            formalizarPrestamoService.formalizar(
                solicitud,
                tipoPrestamo,
                observacion
            );

        notificacionPrestamoPort.notificarDecision(
            solicitud.getUsuarioId(),
            resultado.getSolicitudId(),
            EstadoSolicitud.APROBADA
        );

        return ResultadoEvaluacionManual.builder()
            .solicitudId(resultado.getSolicitudId())
            .estado(EstadoSolicitud.APROBADA)
            .prestamoId(resultado.getPrestamoId())
            .mensaje("Solicitud aprobada y préstamo creado correctamente")
            .build();
    }

    private void validarEstadoActual(SolicitudPrestamo solicitud) {
        EstadoSolicitud estado = solicitud.getEstado();

        boolean estadoPermitido =
            estado == EstadoSolicitud.PENDIENTE_REVISION
                || estado == EstadoSolicitud.REVISION_MANUAL;

        if (!estadoPermitido) {
            throw new IllegalStateException(
                "La solicitud no puede evaluarse porque su estado actual es: "
                    + estado
            );
        }
    }

    private void validarParametros(
        UUID solicitudId,
        DecisionManual decision
    ) {
        if (solicitudId == null) {
            throw new IllegalArgumentException(
                "El identificador de la solicitud es obligatorio"
            );
        }

        if (decision == null) {
            throw new IllegalArgumentException(
                "La decisión manual es obligatoria"
            );
        }
    }
}
