package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.dto.ResultadoFormalizacionPrestamo;
import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.port.out.CuotaPlanPagoRepositoryPort;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoPrestamo;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.domain.model.PlanPagos;
import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.domain.model.TipoPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FormalizarPrestamoServiceTest {

    @Mock private SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    @Mock private PrestamoRepositoryPort prestamoRepositoryPort;
    @Mock private CuotaPlanPagoRepositoryPort cuotaPlanPagoRepositoryPort;
    @Mock private GenerarPlanPagosUseCase generarPlanPagosUseCase;

    private FormalizarPrestamoService service;

    private UUID solicitudId;
    private UUID tipoPrestamoId;

    @BeforeEach
    void setUp() {
        service = new FormalizarPrestamoService(
            solicitudPrestamoRepositoryPort,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            generarPlanPagosUseCase
        );

        solicitudId = UUID.randomUUID();
        tipoPrestamoId = UUID.randomUUID();
    }

    @Test
    void debeFallarCuandoSolicitudEsNula() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.formalizar(null, crearTipoPrestamo(true), "obs")
        );
        verifyNoInteractions(solicitudPrestamoRepositoryPort, prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort, generarPlanPagosUseCase);
    }

    @Test
    void debeFallarCuandoTipoPrestamoEsNulo() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.formalizar(crearSolicitud(), null, "obs")
        );
        verifyNoInteractions(solicitudPrestamoRepositoryPort, prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort, generarPlanPagosUseCase);
    }

    @Test
    void debeFallarCuandoTipoPrestamoNoEstaActivo() {
        assertThrows(
            IllegalStateException.class,
            () -> service.formalizar(crearSolicitud(), crearTipoPrestamo(false), "obs")
        );
        verifyNoInteractions(solicitudPrestamoRepositoryPort, prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort, generarPlanPagosUseCase);
    }

    @Test
    void debeFormalizarElPrestamoCorrectamente() {
        SolicitudPrestamo solicitud = crearSolicitud();
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true);
        UUID prestamoId = UUID.randomUUID();

        CuotaPlanPago cuota = CuotaPlanPago.builder().numeroCuota(1).build();
        PlanPagos plan = PlanPagos.builder()
            .cuotaMensual(new BigDecimal("250000.00"))
            .cuotas(List.of(cuota))
            .build();

        when(generarPlanPagosUseCase.generar(any(), any(), anyInt(), any())).thenReturn(plan);
        when(solicitudPrestamoRepositoryPort.guardar(any(SolicitudPrestamo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        Prestamo prestamoGuardado = Prestamo.builder()
            .id(prestamoId)
            .solicitudId(solicitudId)
            .build();
        when(prestamoRepositoryPort.guardar(any(Prestamo.class))).thenReturn(prestamoGuardado);

        ResultadoFormalizacionPrestamo resultado =
            service.formalizar(solicitud, tipoPrestamo, "Cumple requisitos");

        assertEquals(solicitudId, resultado.getSolicitudId());
        assertEquals(prestamoId, resultado.getPrestamoId());

        assertEquals(EstadoSolicitud.APROBADA, solicitud.getEstado());
        assertEquals("Cumple requisitos", solicitud.getObservacionDecision());
        assertNotNull(solicitud.getFechaDecision());

        verify(cuotaPlanPagoRepositoryPort).guardarTodas(prestamoId, plan.getCuotas());
        verify(solicitudPrestamoRepositoryPort).guardar(solicitud);
        verify(prestamoRepositoryPort).guardar(any(Prestamo.class));
    }

    @Test
    void noValidaElEstadoActualDeLaSolicitudAntesDeFormalizar() {
        // Documenta un hueco de diseño: FormalizarPrestamoService confía en que
        // quien lo invoca ya validó el estado. Si se llama directamente sobre
        // una solicitud ya RECHAZADA, igual la formaliza sin protestar.
        SolicitudPrestamo solicitudYaRechazada = crearSolicitud();
        solicitudYaRechazada.setEstado(EstadoSolicitud.RECHAZADA);
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true);

        PlanPagos plan = PlanPagos.builder()
            .cuotaMensual(new BigDecimal("250000.00"))
            .cuotas(List.of())
            .build();
        when(generarPlanPagosUseCase.generar(any(), any(), anyInt(), any())).thenReturn(plan);
        when(solicitudPrestamoRepositoryPort.guardar(any(SolicitudPrestamo.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        when(prestamoRepositoryPort.guardar(any(Prestamo.class)))
            .thenReturn(Prestamo.builder().id(UUID.randomUUID()).build());

        service.formalizar(solicitudYaRechazada, tipoPrestamo, "obs");

        // El estado termina en APROBADA aunque venía RECHAZADA — comportamiento
        // actual, no deseable a largo plazo.
        assertEquals(EstadoSolicitud.APROBADA, solicitudYaRechazada.getEstado());
    }

    private SolicitudPrestamo crearSolicitud() {
        return SolicitudPrestamo.builder()
            .id(solicitudId)
            .usuarioId(UUID.randomUUID())
            .tipoPrestamoId(tipoPrestamoId)
            .monto(new BigDecimal("5000000.00"))
            .plazoMeses(24)
            .estado(EstadoSolicitud.REVISION_MANUAL)
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
