package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.dto.ResultadoEvaluacionAutomatica;
import com.prestamosfacil.application.dto.ResultadoFormalizacionPrestamo;
import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.port.out.EvaluacionAutomaticaRepositoryPort;
import com.prestamosfacil.application.port.out.NotificacionPrestamoPort;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.domain.model.PlanPagos;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.domain.model.TipoPrestamo;
import com.prestamosfacil.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluarSolicitudAutomaticaServiceTest {

    @Mock private SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    @Mock private UsuarioRepositoryPort usuarioRepositoryPort;
    @Mock private TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;
    @Mock private PrestamoRepositoryPort prestamoRepositoryPort;
    @Mock private EvaluacionAutomaticaRepositoryPort evaluacionAutomaticaRepositoryPort;
    @Mock private GenerarPlanPagosUseCase generarPlanPagosUseCase;
    @Mock private FormalizarPrestamoService formalizarPrestamoService;
    @Mock private NotificacionPrestamoPort notificacionPrestamoPort;

    private EvaluarSolicitudAutomaticaService service;

    private UUID solicitudId;
    private UUID usuarioId;
    private UUID tipoPrestamoId;

    @BeforeEach
    void setUp() {
        service = new EvaluarSolicitudAutomaticaService(
            solicitudPrestamoRepositoryPort,
            usuarioRepositoryPort,
            tipoPrestamoRepositoryPort,
            prestamoRepositoryPort,
            evaluacionAutomaticaRepositoryPort,
            generarPlanPagosUseCase,
            formalizarPrestamoService,
            notificacionPrestamoPort
        );

        solicitudId = UUID.randomUUID();
        usuarioId = UUID.randomUUID();
        tipoPrestamoId = UUID.randomUUID();
    }

    // ---------- VALIDACIONES PREVIAS ----------

    @Test
    void debeFallarCuandoSolicitudIdEsNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.evaluar(null));

        verifyNoInteractions(
            solicitudPrestamoRepositoryPort, usuarioRepositoryPort, tipoPrestamoRepositoryPort,
            prestamoRepositoryPort, evaluacionAutomaticaRepositoryPort,
            generarPlanPagosUseCase, formalizarPrestamoService, notificacionPrestamoPort
        );
    }

    @Test
    void debeFallarCuandoSolicitudNoExiste() {
        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.evaluar(solicitudId));
        verifyNoInteractions(usuarioRepositoryPort, evaluacionAutomaticaRepositoryPort);
    }

    @Test
    void debeFallarCuandoEstadoNoEsPendienteRevision() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.REVISION_MANUAL);
        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.evaluar(solicitudId)
        );
        assertTrue(ex.getMessage().contains("REVISION_MANUAL"));
        verifyNoInteractions(usuarioRepositoryPort);
    }

    @Test
    void debeFallarCuandoUsuarioNoExiste() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.evaluar(solicitudId));
        verifyNoInteractions(tipoPrestamoRepositoryPort, evaluacionAutomaticaRepositoryPort);
    }

    @Test
    void debeFallarCuandoSalarioEsNuloOInvalido() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(null);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalStateException.class, () -> service.evaluar(solicitudId));
        verifyNoInteractions(tipoPrestamoRepositoryPort);
    }

    @Test
    void debeFallarCuandoTipoPrestamoNoExiste() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.evaluar(solicitudId));
        verifyNoInteractions(evaluacionAutomaticaRepositoryPort, prestamoRepositoryPort);
    }

    @Test
    void debeFallarCuandoTipoPrestamoEstaInactivo() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(false, true);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId))
            .thenReturn(Optional.of(tipoPrestamo));

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.evaluar(solicitudId)
        );
        assertTrue(ex.getMessage().contains("activo"));
    }

    @Test
    void debeFallarCuandoTipoPrestamoNoTieneValidacionAutomatica() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true, false);

        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId))
            .thenReturn(Optional.of(tipoPrestamo));

        assertThrows(IllegalStateException.class, () -> service.evaluar(solicitudId));
    }

    @Test
    void debeFallarCuandoCuotaMensualCalculadaEsInvalida() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true, true);
        PlanPagos planInvalido = PlanPagos.builder().cuotaMensual(BigDecimal.ZERO).build();

        prepararDependenciasBase(solicitud, usuario, tipoPrestamo);
        when(generarPlanPagosUseCase.generar(any(), any(), anyInt(), any()))
            .thenReturn(planInvalido);

        assertThrows(IllegalStateException.class, () -> service.evaluar(solicitudId));
        verifyNoInteractions(evaluacionAutomaticaRepositoryPort);
    }

    // ---------- DECISIONES DEL SWITCH ----------

    @Test
    void debeAprobarYFormalizarCuandoDecisionEsAprobada() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true, true);
        UUID prestamoId = UUID.randomUUID();

        prepararDependenciasBase(solicitud, usuario, tipoPrestamo);
        prepararPlanValido();
        when(evaluacionAutomaticaRepositoryPort.evaluar(any(), any(), any(), any()))
            .thenReturn(EstadoSolicitud.APROBADA);
        when(formalizarPrestamoService.formalizar(solicitud, tipoPrestamo,
            "Solicitud aprobada mediante evaluación automática"))
            .thenReturn(ResultadoFormalizacionPrestamo.builder()
                .solicitudId(solicitudId)
                .prestamoId(prestamoId)
                .build());

        ResultadoEvaluacionAutomatica resultado = service.evaluar(solicitudId);

        assertEquals(EstadoSolicitud.APROBADA, resultado.getEstado());
        assertEquals(prestamoId, resultado.getPrestamoId());
        verify(solicitudPrestamoRepositoryPort, never()).guardar(any());
        verify(notificacionPrestamoPort)
            .notificarDecision(usuarioId, solicitudId, EstadoSolicitud.APROBADA);
    }

    @Test
    void debeEnviarARevisionManualCuandoDecisionLoIndica() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true, true);

        prepararDependenciasBase(solicitud, usuario, tipoPrestamo);
        prepararPlanValido();
        when(evaluacionAutomaticaRepositoryPort.evaluar(any(), any(), any(), any()))
            .thenReturn(EstadoSolicitud.REVISION_MANUAL);
        when(solicitudPrestamoRepositoryPort.guardar(any(SolicitudPrestamo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ResultadoEvaluacionAutomatica resultado = service.evaluar(solicitudId);

        assertEquals(EstadoSolicitud.REVISION_MANUAL, resultado.getEstado());
        assertNull(resultado.getPrestamoId());
        verifyNoInteractions(formalizarPrestamoService);
        verify(notificacionPrestamoPort)
            .notificarDecision(usuarioId, solicitudId, EstadoSolicitud.REVISION_MANUAL);
    }

    @Test
    void debeRechazarCuandoDecisionLoIndica() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true, true);

        prepararDependenciasBase(solicitud, usuario, tipoPrestamo);
        prepararPlanValido();
        when(evaluacionAutomaticaRepositoryPort.evaluar(any(), any(), any(), any()))
            .thenReturn(EstadoSolicitud.RECHAZADA);
        when(solicitudPrestamoRepositoryPort.guardar(any(SolicitudPrestamo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        ResultadoEvaluacionAutomatica resultado = service.evaluar(solicitudId);

        assertEquals(EstadoSolicitud.RECHAZADA, resultado.getEstado());
        assertNull(resultado.getPrestamoId());
        verifyNoInteractions(formalizarPrestamoService);
    }

    @Test
    void debeFallarCuandoDecisionEsNula() {
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true, true);

        prepararDependenciasBase(solicitud, usuario, tipoPrestamo);
        prepararPlanValido();
        when(evaluacionAutomaticaRepositoryPort.evaluar(any(), any(), any(), any()))
            .thenReturn(null);

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.evaluar(solicitudId)
        );
        assertTrue(ex.getMessage().contains("no retornó una decisión"));
        verifyNoInteractions(formalizarPrestamoService, notificacionPrestamoPort);
    }

    @Test
    void debeFallarCuandoDecisionNoEsUnaDeLasPermitidas() {
        // PENDIENTE_REVISION es un valor válido del enum pero no está contemplado
        // como decisión posible dentro del switch -> cae al "default".
        SolicitudPrestamo solicitud = crearSolicitud(EstadoSolicitud.PENDIENTE_REVISION);
        Usuario usuario = crearUsuario(new BigDecimal("3000000"));
        TipoPrestamo tipoPrestamo = crearTipoPrestamo(true, true);

        prepararDependenciasBase(solicitud, usuario, tipoPrestamo);
        prepararPlanValido();
        when(evaluacionAutomaticaRepositoryPort.evaluar(any(), any(), any(), any()))
            .thenReturn(EstadoSolicitud.PENDIENTE_REVISION);

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> service.evaluar(solicitudId)
        );
        assertTrue(ex.getMessage().contains("Decisión automática no permitida"));
    }

    // ---------- HELPERS ----------

    private void prepararDependenciasBase(
        SolicitudPrestamo solicitud, Usuario usuario, TipoPrestamo tipoPrestamo
    ) {
        when(solicitudPrestamoRepositoryPort.buscarPorId(solicitudId))
            .thenReturn(Optional.of(solicitud));
        when(usuarioRepositoryPort.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));
        when(tipoPrestamoRepositoryPort.buscarPorId(tipoPrestamoId))
            .thenReturn(Optional.of(tipoPrestamo));
        when(prestamoRepositoryPort.obtenerDeudaMensualActiva(usuarioId))
            .thenReturn(BigDecimal.ZERO);
    }

    private void prepararPlanValido() {
        PlanPagos plan = PlanPagos.builder()
            .cuotaMensual(new BigDecimal("250000.00"))
            .cuotas(List.of())
            .build();
        when(generarPlanPagosUseCase.generar(any(), any(), anyInt(), any()))
            .thenReturn(plan);
    }

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

    private Usuario crearUsuario(BigDecimal salarioBase) {
        return Usuario.builder()
            .id(usuarioId)
            .salarioBase(salarioBase)
            .build();
    }

    private TipoPrestamo crearTipoPrestamo(boolean activo, boolean validacionAutomatica) {
        return TipoPrestamo.builder()
            .id(tipoPrestamoId)
            .nombre("VEHICULO")
            .tasaAnual(new BigDecimal("14.5000"))
            .validacionAutomatica(validacionAutomatica)
            .activo(activo)
            .fechaCreacion(Instant.now())
            .build();
    }
}
