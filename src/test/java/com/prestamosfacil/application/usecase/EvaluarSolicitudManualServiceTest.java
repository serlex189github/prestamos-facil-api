package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.port.out.CuotaPlanPagoRepositoryPort;
import com.prestamosfacil.application.port.out.NotificacionPrestamoPort;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.DecisionManual;
import com.prestamosfacil.domain.enums.EstadoPrestamo;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.domain.model.PlanPagos;
import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.domain.model.ResultadoEvaluacionManual;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.domain.model.TipoPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluarSolicitudManualServiceTest {

    @Mock
    private SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;

    @Mock
    private TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;

    @Mock
    private PrestamoRepositoryPort prestamoRepositoryPort;

    @Mock
    private CuotaPlanPagoRepositoryPort cuotaPlanPagoRepositoryPort;

    @Mock
    private GenerarPlanPagosUseCase generarPlanPagosUseCase;

    @Mock
    private NotificacionPrestamoPort notificacionPrestamoPort;

    private EvaluarSolicitudManualService service;

    private UUID solicitudId;
    private UUID usuarioId;
    private UUID tipoPrestamoId;

    @BeforeEach
    void setUp() {
        service = new EvaluarSolicitudManualService(
            solicitudPrestamoRepositoryPort,
            tipoPrestamoRepositoryPort,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            generarPlanPagosUseCase,
            notificacionPrestamoPort
        );

        solicitudId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        tipoPrestamoId = UUID.randomUUID();
    }

    @Test
    void debeAprobarSolicitudCrearPrestamoYGuardarCuotas() {
        SolicitudPrestamo solicitud = crearSolicitud(
            EstadoSolicitud.PENDIENTE_REVISION
        );

        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true);

        CuotaPlanPago cuota = mock(CuotaPlanPago.class);
        List<CuotaPlanPago> cuotas = List.of(cuota);

        PlanPagos planPagos = mock(PlanPagos.class);
        when(planPagos.getCuotaMensual())
            .thenReturn(new BigDecimal("241274.87"));
        when(planPagos.getCuotas()).thenReturn(cuotas);

        UUID prestamoId = UUID.randomUUID();

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));

        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId))
            .thenReturn(Optional.of(tipoPrestamo));

        when(generarPlanPagosUseCase.generar(
            eq(new BigDecimal("5000000.00")),
            eq(new BigDecimal("14.5000")),
            eq(24),
            any(LocalDate.class)
        )).thenReturn(planPagos);

        when(solicitudPrestamoRepositoryPort.guardar(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        when(prestamoRepositoryPort.guardar(any()))
            .thenAnswer(invocation -> {
                Prestamo prestamo = invocation.getArgument(0);

                return Prestamo.builder()
                    .id(prestamoId)
                    .solicitudId(prestamo.getSolicitudId())
                    .montoOriginal(prestamo.getMontoOriginal())
                    .saldoPendiente(prestamo.getSaldoPendiente())
                    .tasaAnual(prestamo.getTasaAnual())
                    .plazoMeses(prestamo.getPlazoMeses())
                    .cuotaMensual(prestamo.getCuotaMensual())
                    .estado(prestamo.getEstado())
                    .fechaAprobacion(prestamo.getFechaAprobacion())
                    .fechaPrimerPago(prestamo.getFechaPrimerPago())
                    .build();
            });

        when(cuotaPlanPagoRepositoryPort.guardarTodas(prestamoId, cuotas))
            .thenReturn(cuotas);

        ResultadoEvaluacionManual resultado = service.evaluar(
            solicitudId,
            DecisionManual.APROBAR,
            "Cumple los criterios"
        );

        assertEquals(solicitudId, resultado.getSolicitudId());
        assertEquals(EstadoSolicitud.APROBADA, resultado.getEstado());
        assertEquals(prestamoId, resultado.getPrestamoId());

        assertEquals(EstadoSolicitud.APROBADA, solicitud.getEstado());
        assertEquals(
            "Cumple los criterios",
            solicitud.getObservacionDecision()
        );
        assertNotNull(solicitud.getFechaDecision());

        ArgumentCaptor<Prestamo> prestamoCaptor =
            ArgumentCaptor.forClass(Prestamo.class);

        verify(prestamoRepositoryPort).guardar(prestamoCaptor.capture());

        Prestamo prestamoCreado = prestamoCaptor.getValue();

        assertNull(prestamoCreado.getId());
        assertEquals(solicitudId, prestamoCreado.getSolicitudId());
        assertEquals(EstadoPrestamo.ACTIVO, prestamoCreado.getEstado());

        verify(cuotaPlanPagoRepositoryPort)
            .guardarTodas(prestamoId, cuotas);

        verify(notificacionPrestamoPort).notificarDecision(
            usuarioId,
            solicitudId,
            EstadoSolicitud.APROBADA
        );
    }

    @Test
    void debeRechazarSolicitudSinCrearPrestamo() {
        SolicitudPrestamo solicitud = crearSolicitud(
            EstadoSolicitud.REVISION_MANUAL
        );

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));

        when(solicitudPrestamoRepositoryPort.guardar(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoEvaluacionManual resultado = service.evaluar(
            solicitudId,
            DecisionManual.RECHAZAR,
            "Capacidad de pago insuficiente"
        );

        assertEquals(solicitudId, resultado.getSolicitudId());
        assertEquals(EstadoSolicitud.RECHAZADA, resultado.getEstado());
        assertNull(resultado.getPrestamoId());

        assertEquals(EstadoSolicitud.RECHAZADA, solicitud.getEstado());
        assertNotNull(solicitud.getFechaDecision());

        verifyNoInteractions(
            tipoPrestamoRepositoryPort,
            generarPlanPagosUseCase,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort
        );

        verify(notificacionPrestamoPort).notificarDecision(
            usuarioId,
            solicitudId,
            EstadoSolicitud.RECHAZADA
        );
    }

    @Test
    void debeFallarCuandoSolicitudNoExiste() {
        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.evaluar(
                solicitudId,
                DecisionManual.APROBAR,
                "Aprobación"
            )
        );

        assertTrue(exception.getMessage().contains(solicitudId.toString()));

        verifyNoInteractions(
            tipoPrestamoRepositoryPort,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            generarPlanPagosUseCase,
            notificacionPrestamoPort
        );
    }

    @Test
    void debeFallarCuandoSolicitudYaFueEvaluada() {
        SolicitudPrestamo solicitud = crearSolicitud(
            EstadoSolicitud.APROBADA
        );

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.evaluar(
                solicitudId,
                DecisionManual.APROBAR,
                "Intento duplicado"
            )
        );

        assertTrue(
            exception.getMessage().contains("APROBADA")
        );

        verifyNoInteractions(
            tipoPrestamoRepositoryPort,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            generarPlanPagosUseCase,
            notificacionPrestamoPort
        );
    }

    @Test
    void debeFallarCuandoTipoPrestamoEstaInactivo() {
        SolicitudPrestamo solicitud = crearSolicitud(
            EstadoSolicitud.PENDIENTE_REVISION
        );

        TipoPrestamo tipoPrestamo = crearTipoPrestamo(false);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));

        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId))
            .thenReturn(Optional.of(tipoPrestamo));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> service.evaluar(
                solicitudId,
                DecisionManual.APROBAR,
                "Aprobación"
            )
        );

        assertEquals(
            "El tipo de préstamo asociado no se encuentra activo",
            exception.getMessage()
        );

        verifyNoInteractions(
            generarPlanPagosUseCase,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            notificacionPrestamoPort
        );
    }

    @Test
    void debeFallarCuandoSolicitudIdEsNulo() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.evaluar(
                null,
                DecisionManual.APROBAR,
                "Aprobación"
            )
        );

        verifyNoInteractions(
            solicitudPrestamoRepositoryPort,
            tipoPrestamoRepositoryPort,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            generarPlanPagosUseCase,
            notificacionPrestamoPort
        );
    }

    private SolicitudPrestamo crearSolicitud(
        EstadoSolicitud estado
    ) {
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
