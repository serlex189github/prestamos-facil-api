package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.port.out.CuotaPlanPagoRepositoryPort;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoPrestamo;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PlanPagos;
import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.application.dto.ResultadoFormalizacionPrestamo;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.domain.model.TipoPrestamo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
public class FormalizarPrestamoService {

    private final SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    private final PrestamoRepositoryPort prestamoRepositoryPort;
    private final CuotaPlanPagoRepositoryPort cuotaPlanPagoRepositoryPort;
    private final GenerarPlanPagosUseCase generarPlanPagosUseCase;

    public ResultadoFormalizacionPrestamo formalizar(
        SolicitudPrestamo solicitud,
        TipoPrestamo tipoPrestamo,
        String observacion
    ) {
        validarParametros(solicitud, tipoPrestamo);
        validarTipoPrestamo(tipoPrestamo);

        LocalDate fechaPrimerPago = LocalDate.now().plusMonths(1);

        PlanPagos planPagos = generarPlanPagosUseCase.generar(
            solicitud.getMonto(),
            tipoPrestamo.getTasaAnual(),
            solicitud.getPlazoMeses(),
            fechaPrimerPago
        );

        Instant fechaAprobacion = Instant.now();

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaDecision(fechaAprobacion);
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
            .fechaAprobacion(fechaAprobacion)
            .fechaPrimerPago(fechaPrimerPago)
            .build();

        Prestamo prestamoGuardado =
            prestamoRepositoryPort.guardar(prestamo);

        cuotaPlanPagoRepositoryPort.guardarTodas(
            prestamoGuardado.getId(),
            planPagos.getCuotas()
        );

        log.debug(
            "Préstamo formalizado. solicitudId={}, prestamoId={}",
            solicitudGuardada.getId(),
            prestamoGuardado.getId()
        );

        return ResultadoFormalizacionPrestamo.builder()
            .solicitudId(solicitudGuardada.getId())
            .prestamoId(prestamoGuardado.getId())
            .build();
    }

    private void validarParametros(
        SolicitudPrestamo solicitud,
        TipoPrestamo tipoPrestamo
    ) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                "La solicitud de préstamo es obligatoria"
            );
        }

        if (tipoPrestamo == null) {
            throw new IllegalArgumentException(
                "El tipo de préstamo es obligatorio"
            );
        }
    }

    private void validarTipoPrestamo(TipoPrestamo tipoPrestamo) {
        if (!Boolean.TRUE.equals(tipoPrestamo.getActivo())) {
            throw new IllegalStateException(
                "El tipo de préstamo asociado no se encuentra activo"
            );
        }
    }
}
