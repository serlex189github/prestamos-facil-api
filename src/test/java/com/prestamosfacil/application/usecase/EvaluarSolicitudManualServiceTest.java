package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.dto.ResultadoEvaluacionManual;
import com.prestamosfacil.application.dto.ResultadoFormalizacionPrestamo;
import com.prestamosfacil.application.port.out.NotificacionPrestamoPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.DecisionManual;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.domain.model.TipoPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluarSolicitudManualServiceTest {

    @Mock
    private SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;

    @Mock
    private TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;

    @Mock
    private NotificacionPrestamoPort notificacionPrestamoPort;

    @Mock
    private FormalizarPrestamoService formalizarPrestamoService;

    private EvaluarSolicitudManualService service;

    private UUID solicitudId;
    private UUID usuarioId;
    private UUID tipoPrestamoId;

    @BeforeEach
    void setUp() {
        service = new EvaluarSolicitudManualService(
            solicitudPrestamoRepositoryPort,
            tipoPrestamoRepositoryPort,
            notificacionPrestamoPort,
            formalizarPrestamoService
        );

        solicitudId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        tipoPrestamoId = UUID.randomUUID();
    }

    // ---------- RECHAZAR ----------

    @Test
    void debeRechazarSolicitudSinFormalizarPrestamo() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.REVISION_MANUAL);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(solicitudPrestamoRepositoryPort.guardar(any(SolicitudPrestamo.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoEvaluacionManual resultado = service.evaluar(
            solicitudId, DecisionManual.RECHAZAR, "Capacidad de pago insuficiente"
        );

        assertEquals(solicitudId, resultado.getSolicitudId());
        assertEquals(EstadoSolicitud.RECHAZADA, resultado.getEstado());
        assertNull(resultado.getPrestamoId());
        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
        assertEquals("Capacidad de pago insuficiente", solicitud.getObservacionDecision());
        assertNotNull(solicitud.getFechaDecision());

        verify(solicitudPrestamoRepositoryPort).guardar(solicitud);
        verifyNoInteractions(tipoPrestamoRepositoryPort, formalizarPrestamoService);
        verify(notificacionPrestamoPort)
            .notificarDecision(usuarioId, solicitudId, EstadoSolicitud.RECHAZADA);
    }

    // ---------- APROBAR (camino feliz — antes NO existía) ----------

    @Test
    void debeAprobarSolicitudYFormalizarPrestamo() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.REVISION_MANUAL);
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true);
        UUID prestamoId = UUID.randomUUID();

        ResultadoFormalizacionPrestamo resultadoFormalizacion =
            ResultadoFormalizacionPrestamo.builder()
                .solicitudId(solicitudId)
                .prestamoId(prestamoId)
                .build();

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId))
            .thenReturn(Optional.of(tipoPrestamo));
        when(formalizarPrestamoService.formalizar(solicitud, tipoPrestamo, "Cumple requisitos"))
            .thenReturn(resultadoFormalizacion);

        ResultadoEvaluacionManual resultado = service.evaluar(
            solicitudId, DecisionManual.APROBAR, "Cumple requisitos"
        );

        assertEquals(solicitudId, resultado.getSolicitudId());
        assertEquals(EstadoSolicitud.APROBADA, resultado.getEstado());
        assertEquals(prestamoId, resultado.getPrestamoId());
        assertEquals("Solicitud aprobada y préstamo creado correctamente", resultado.getMensaje());

        verify(formalizarPrestamoService).formalizar(solicitud, tipoPrestamo, "Cumple requisitos");
        verify(solicitudPrestamoRepositoryPort, never()).guardar(any());
        verify(notificacionPrestamoPort)
            .notificarDecision(usuarioId, solicitudId, EstadoSolicitud.APROBADA);
    }

    @Test
    void debeFallarAlAprobarCuandoTipoPrestamoNoExiste() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.REVISION_MANUAL);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId))
            .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.evaluar(solicitudId, DecisionManual.APROBAR, "Cumple requisitos")
        );

        assertTrue(exception.getMessage().contains(tipoPrestamoId.toString()));
        verifyNoInteractions(formalizarPrestamoService, notificacionPrestamoPort);
    }

    // ---------- ESTADOS VÁLIDOS (antes solo se probaba REVISION_MANUAL) ----------

    @ParameterizedTest
    @EnumSource(value = EstadoSolicitud.class, names = {"PENDIENTE_REVISION", "REVISION_MANUAL"})
    void debePermitirEvaluacionEnEstadosValidos(EstadoSolicitud estadoInicial) {
        SolicitudPrestamo solicitud = crearSolicitud(estadoInicial);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(solicitudPrestamoRepositoryPort.guardar(any(SolicitudPrestamo.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoEvaluacionManual resultado = service.evaluar(
            solicitudId, DecisionManual.RECHAZAR, "Observación"
        );

        assertEquals(EstadoSolicitud.RECHAZADA, resultado.getEstado());
    }

    @Test
    void debeFallarCuandoSolicitudYaFueEvaluada() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.APROBADA);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.evaluar(solicitudId, DecisionManual.APROBAR, "Intento duplicado")
        );

        assertTrue(exception.getMessage().contains("APROBADA"));
        verify(solicitudPrestamoRepositoryPort, never()).guardar(any());
        verifyNoInteractions(tipoPrestamoRepositoryPort, notificacionPrestamoPort, formalizarPrestamoService);
    }

    // ---------- VALIDACIÓN DE PARÁMETROS ----------

    @Test
    void debeFallarCuandoSolicitudNoExiste() {
        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.evaluar(solicitudId, DecisionManual.APROBAR, "Aprobación")
        );

        assertTrue(exception.getMessage().contains(solicitudId.toString()));
        verifyNoInteractions(tipoPrestamoRepositoryPort, notificacionPrestamoPort, formalizarPrestamoService);
    }

    @Test
    void debeFallarCuandoSolicitudIdEsNulo() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.evaluar(null, DecisionManual.APROBAR, "Aprobación")
        );

        verifyNoInteractions(
            solicitudPrestamoRepositoryPort, tipoPrestamoRepositoryPort,
            notificacionPrestamoPort, formalizarPrestamoService
        );
    }

    @Test
    void debeFallarCuandoDecisionEsNula() {
        // Antes NO se probaba esta rama de validarParametros().
        assertThrows(
            IllegalArgumentException.class,
            () -> service.evaluar(solicitudId, null, "Sin decisión")
        );

        verifyNoInteractions(
            solicitudPrestamoRepositoryPort, tipoPrestamoRepositoryPort,
            notificacionPrestamoPort, formalizarPrestamoService
        );
    }

    // ---------- HELPERS ----------

    private SolicitudPrestamo crearSolicitud(EstadoSolicitud estado) {
        return SolicitudPrestamo.builder()
            .id(solicitudId)
            .usuarioId(usuarioId)
            .tipoPrestamoId(tipoPrestamoId)
            .monto(new BigDecimal("5000000.00"))
            .plazoMeses(24)
            .estado(estado)
            .fechaSolicitud(Instant.now())
            .build();
    }

    private TipoPrestamo crearTipoPrestamo(boolean activo) {
        return TipoPrestamo.builder()
            .id(tipoPrestamoId)
            .nombre("VEHICULO")
            .tasaAnual(new BigDecimal("14.5000"))
            .validacionAutomatica(true)
            .activo(activo)
            .fechaCreacion(Instant.now())
            .build();
    }
}
