package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.dto.ResultadoEvaluacionAutomatica;
import com.prestamosfacil.application.dto.ResultadoFormalizacionPrestamo;
import com.prestamosfacil.application.port.in.EvaluarSolicitudAutomaticaUseCase;
import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.port.out.EvaluacionAutomaticaRepositoryPort;
import com.prestamosfacil.application.port.out.NotificacionPrestamoPort;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PlanPagos;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.domain.model.TipoPrestamo;
import com.prestamosfacil.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class EvaluarSolicitudAutomaticaService
    implements EvaluarSolicitudAutomaticaUseCase {

    private final SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;
    private final PrestamoRepositoryPort prestamoRepositoryPort;
    private final EvaluacionAutomaticaRepositoryPort evaluacionAutomaticaRepositoryPort;
    private final GenerarPlanPagosUseCase generarPlanPagosUseCase;
    private final FormalizarPrestamoService formalizarPrestamoService;
    private final NotificacionPrestamoPort notificacionPrestamoPort;

    @Override
    @Transactional
    public ResultadoEvaluacionAutomatica evaluar(UUID solicitudId) {
        validarSolicitudId(solicitudId);

        SolicitudPrestamo solicitud = buscarSolicitud(solicitudId);
        validarEstadoActual(solicitud);

        Usuario usuario = buscarUsuario(solicitud.getUsuarioId());
        validarSalario(usuario);

        TipoPrestamo tipoPrestamo =
            buscarTipoPrestamo(solicitud.getTipoPrestamoId());

        validarTipoPrestamo(tipoPrestamo);

        BigDecimal deudaMensualActual =
            prestamoRepositoryPort.obtenerDeudaMensualActiva(
                solicitud.getUsuarioId()
            );

        BigDecimal cuotaNueva =
            calcularCuotaNueva(solicitud, tipoPrestamo);

        EstadoSolicitud decision =
            evaluacionAutomaticaRepositoryPort.evaluar(
                usuario.getSalarioBase(),
                deudaMensualActual,
                cuotaNueva,
                solicitud.getMonto()
            );

        log.info(
            "Evaluación automática realizada. solicitudId={}, "
                + "salarioBase={}, deudaActual={}, cuotaNueva={}, decision={}",
            solicitud.getId(),
            usuario.getSalarioBase(),
            deudaMensualActual,
            cuotaNueva,
            decision
        );

        return procesarDecision(
            solicitud,
            tipoPrestamo,
            decision
        );
    }

    private ResultadoEvaluacionAutomatica procesarDecision(
        SolicitudPrestamo solicitud,
        TipoPrestamo tipoPrestamo,
        EstadoSolicitud decision
    ) {
        if (decision == null) {
            throw new IllegalStateException(
                "El procedimiento almacenado no retornó una decisión"
            );
        }

        return switch (decision) {
            case APROBADA -> aprobar(solicitud, tipoPrestamo);

            case REVISION_MANUAL -> actualizarEstado(
                solicitud,
                EstadoSolicitud.REVISION_MANUAL,
                "Solicitud enviada a revisión manual por evaluación automática"
            );

            case RECHAZADA -> actualizarEstado(
                solicitud,
                EstadoSolicitud.RECHAZADA,
                "Solicitud rechazada por evaluación automática"
            );

            default -> throw new IllegalStateException(
                "Decisión automática no permitida: " + decision
            );
        };
    }

    private ResultadoEvaluacionAutomatica aprobar(
        SolicitudPrestamo solicitud,
        TipoPrestamo tipoPrestamo
    ) {
        ResultadoFormalizacionPrestamo resultadoFormalizacion =
            formalizarPrestamoService.formalizar(
                solicitud,
                tipoPrestamo,
                "Solicitud aprobada mediante evaluación automática"
            );

        notificacionPrestamoPort.notificarDecision(
            solicitud.getUsuarioId(),
            solicitud.getId(),
            EstadoSolicitud.APROBADA
        );

        return ResultadoEvaluacionAutomatica.builder()
            .solicitudId(resultadoFormalizacion.getSolicitudId())
            .prestamoId(resultadoFormalizacion.getPrestamoId())
            .estado(EstadoSolicitud.APROBADA)
            .mensaje("Solicitud aprobada mediante evaluación automática")
            .build();
    }

    private ResultadoEvaluacionAutomatica actualizarEstado(
        SolicitudPrestamo solicitud,
        EstadoSolicitud nuevoEstado,
        String observacion
    ) {
        solicitud.setEstado(nuevoEstado);
        solicitud.setFechaDecision(Instant.now());
        solicitud.setObservacionDecision(observacion);

        SolicitudPrestamo solicitudGuardada =
            solicitudPrestamoRepositoryPort.guardar(solicitud);

        notificacionPrestamoPort.notificarDecision(
            solicitudGuardada.getUsuarioId(),
            solicitudGuardada.getId(),
            solicitudGuardada.getEstado()
        );

        return ResultadoEvaluacionAutomatica.builder()
            .solicitudId(solicitudGuardada.getId())
            .prestamoId(null)
            .estado(solicitudGuardada.getEstado())
            .mensaje(observacion)
            .build();
    }

    private BigDecimal calcularCuotaNueva(
        SolicitudPrestamo solicitud,
        TipoPrestamo tipoPrestamo
    ) {
        PlanPagos planPagos = generarPlanPagosUseCase.generar(
            solicitud.getMonto(),
            tipoPrestamo.getTasaAnual(),
            solicitud.getPlazoMeses(),
            LocalDate.now().plusMonths(1)
        );

        BigDecimal cuotaMensual = planPagos.getCuotaMensual();

        if (cuotaMensual == null
            || cuotaMensual.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                "No fue posible calcular una cuota mensual válida"
            );
        }

        return cuotaMensual;
    }

    private SolicitudPrestamo buscarSolicitud(UUID solicitudId) {
        return solicitudPrestamoRepositoryPort
            .buscarPorId(solicitudId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe la solicitud de préstamo con id: "
                        + solicitudId
                )
            );
    }

    private Usuario buscarUsuario(UUID usuarioId) {
        return usuarioRepositoryPort
            .buscarPorId(usuarioId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el usuario con id: " + usuarioId
                )
            );
    }

    private TipoPrestamo buscarTipoPrestamo(UUID tipoPrestamoId) {
        return tipoPrestamoRepositoryPort
            .buscarPorId(tipoPrestamoId)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "No existe el tipo de préstamo con id: "
                        + tipoPrestamoId
                )
            );
    }

    private void validarEstadoActual(SolicitudPrestamo solicitud) {
        if (solicitud.getEstado()
            != EstadoSolicitud.PENDIENTE_REVISION) {

            throw new IllegalStateException(
                "La solicitud no puede evaluarse automáticamente "
                    + "porque su estado actual es: "
                    + solicitud.getEstado()
            );
        }
    }

    private void validarTipoPrestamo(TipoPrestamo tipoPrestamo) {
        if (!Boolean.TRUE.equals(tipoPrestamo.getActivo())) {
            throw new IllegalStateException(
                "El tipo de préstamo asociado no se encuentra activo"
            );
        }

        if (!Boolean.TRUE.equals(
            tipoPrestamo.getValidacionAutomatica()
        )) {
            throw new IllegalStateException(
                "El tipo de préstamo no tiene habilitada "
                    + "la validación automática"
            );
        }
    }

    private void validarSalario(Usuario usuario) {
        if (usuario.getSalarioBase() == null
            || usuario.getSalarioBase()
            .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalStateException(
                "El usuario no tiene un salario base válido"
            );
        }
    }

    private void validarSolicitudId(UUID solicitudId) {
        if (solicitudId == null) {
            throw new IllegalArgumentException(
                "El identificador de la solicitud es obligatorio"
            );
        }
    }
}
