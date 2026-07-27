package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.in.EvaluarSolicitudManualUseCase;
import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.port.out.CuotaPlanPagoRepositoryPort;
import com.prestamosfacil.application.port.out.NotificacionPrestamoPort;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.DecisionManual;
import com.prestamosfacil.domain.enums.EstadoPrestamo;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PlanPagos;
import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.domain.model.ResultadoEvaluacionManual;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.domain.model.TipoPrestamo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EvaluarSolicitudManualService
    implements EvaluarSolicitudManualUseCase {

    private final SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    private final TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;
    private final PrestamoRepositoryPort prestamoRepositoryPort;
    private final CuotaPlanPagoRepositoryPort cuotaPlanPagoRepositoryPort;
    private final GenerarPlanPagosUseCase generarPlanPagosUseCase;
    private final NotificacionPrestamoPort notificacionPrestamoPort;

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

        if (!Boolean.TRUE.equals(tipoPrestamo.getActivo())) {
            throw new IllegalStateException(
                "El tipo de préstamo asociado no se encuentra activo"
            );
        }

        LocalDate fechaPrimerPago = LocalDate.now().plusMonths(1);

        PlanPagos planPagos = generarPlanPagosUseCase.generar(
            solicitud.getMonto(),
            tipoPrestamo.getTasaAnual(),
            solicitud.getPlazoMeses(),
            fechaPrimerPago
        );

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaDecision(Instant.now());
        solicitud.setObservacionDecision(observacion);

        SolicitudPrestamo solicitudGuardada =
            solicitudPrestamoRepositoryPort.guardar(solicitud);

        Prestamo prestamo = Prestamo.builder()
            .id(null)
            .solicitudId(solicitudGuardada.getId())
            .montoOriginal(solicitudGuardada.getMonto())
            .saldoPendiente(solicitudGuardada.getMonto())
            .tasaAnual(tipoPrestamo.getTasaAnual())
            .plazoMeses(solicitudGuardada.getPlazoMeses())
            .cuotaMensual(planPagos.getCuotaMensual())
            .estado(EstadoPrestamo.ACTIVO)
            .fechaAprobacion(Instant.now())
            .fechaPrimerPago(fechaPrimerPago)
            .build();

        Prestamo prestamoGuardado =
            prestamoRepositoryPort.guardar(prestamo);

        cuotaPlanPagoRepositoryPort.guardarTodas(
            prestamoGuardado.getId(),
            planPagos.getCuotas()
        );

        notificacionPrestamoPort.notificarDecision(
            solicitudGuardada.getUsuarioId(),
            solicitudGuardada.getId(),
            solicitudGuardada.getEstado()
        );

        return ResultadoEvaluacionManual.builder()
            .solicitudId(solicitudGuardada.getId())
            .estado(solicitudGuardada.getEstado())
            .prestamoId(prestamoGuardado.getId())
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
