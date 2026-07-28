package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.exception.MontoSolicitudInvalidoException;
import com.prestamosfacil.domain.exception.PlazoSolicitudInvalidoException;
import com.prestamosfacil.domain.exception.TipoPrestamoNoDisponibleException;
import com.prestamosfacil.domain.exception.UsuarioNoEncontradoException;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrarSolicitudPrestamoServiceTest {

    @Mock private SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    @Mock private UsuarioRepositoryPort usuarioRepositoryPort;
    @Mock private TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;

    private RegistrarSolicitudPrestamoService service;

    private UUID usuarioId;
    private UUID tipoPrestamoId;

    @BeforeEach
    void setUp() {
        service = new RegistrarSolicitudPrestamoService(
            solicitudPrestamoRepositoryPort, usuarioRepositoryPort, tipoPrestamoRepositoryPort
        );
        usuarioId = UUID.randomUUID();
        tipoPrestamoId = UUID.randomUUID();
    }

    @Test
    void debeFallarCuandoSolicitudEsNula() {
        assertThrows(IllegalArgumentException.class, () -> service.registrar(null));
        verifyNoInteractions(usuarioRepositoryPort, tipoPrestamoRepositoryPort,
            solicitudPrestamoRepositoryPort);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1000"})
    void debeFallarCuandoMontoNoEsPositivo(String monto) {
        SolicitudPrestamo solicitud = crearSolicitudBase(new BigDecimal(monto), 12);
        assertThrows(MontoSolicitudInvalidoException.class, () -> service.registrar(solicitud));
        verifyNoInteractions(usuarioRepositoryPort, tipoPrestamoRepositoryPort);
    }

    @Test
    void debeFallarCuandoMontoEsNulo() {
        SolicitudPrestamo solicitud = crearSolicitudBase(null, 12);
        assertThrows(MontoSolicitudInvalidoException.class, () -> service.registrar(solicitud));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 73, 100})
    void debeFallarCuandoPlazoEstaFueraDeRango(int plazo) {
        SolicitudPrestamo solicitud = crearSolicitudBase(new BigDecimal("1000000"), plazo);
        assertThrows(PlazoSolicitudInvalidoException.class, () -> service.registrar(solicitud));
    }

    @Test
    void debeFallarCuandoPlazoEsNulo() {
        SolicitudPrestamo solicitud = SolicitudPrestamo.builder()
            .usuarioId(usuarioId)
            .tipoPrestamoId(tipoPrestamoId)
            .monto(new BigDecimal("1000000"))
            .plazoMeses(null)
            .build();
        assertThrows(PlazoSolicitudInvalidoException.class, () -> service.registrar(solicitud));
    }

    @Test
    void debeFallarCuandoUsuarioIdEsNulo() {
        SolicitudPrestamo solicitud = SolicitudPrestamo.builder()
            .usuarioId(null)
            .tipoPrestamoId(tipoPrestamoId)
            .monto(new BigDecimal("1000000"))
            .plazoMeses(12)
            .build();
        assertThrows(UsuarioNoEncontradoException.class, () -> service.registrar(solicitud));
        verifyNoInteractions(usuarioRepositoryPort, tipoPrestamoRepositoryPort);
    }

    @Test
    void debeFallarCuandoTipoPrestamoIdEsNulo() {
        SolicitudPrestamo solicitud = SolicitudPrestamo.builder()
            .usuarioId(usuarioId)
            .tipoPrestamoId(null)
            .monto(new BigDecimal("1000000"))
            .plazoMeses(12)
            .build();
        assertThrows(TipoPrestamoNoDisponibleException.class, () -> service.registrar(solicitud));
    }

    @Test
    void debeFallarCuandoUsuarioNoExiste() {
        SolicitudPrestamo solicitud = crearSolicitudBase(new BigDecimal("1000000"), 12);
        when(usuarioRepositoryPort.existePorId(usuarioId)).thenReturn(false);

        assertThrows(UsuarioNoEncontradoException.class, () -> service.registrar(solicitud));
        verifyNoInteractions(tipoPrestamoRepositoryPort, solicitudPrestamoRepositoryPort);
    }

    @Test
    void debeFallarCuandoTipoPrestamoNoEstaDisponible() {
        SolicitudPrestamo solicitud = crearSolicitudBase(new BigDecimal("1000000"), 12);
        when(usuarioRepositoryPort.existePorId(usuarioId)).thenReturn(true);
        when(tipoPrestamoRepositoryPort.existeActivoPorId(tipoPrestamoId)).thenReturn(false);

        assertThrows(TipoPrestamoNoDisponibleException.class, () -> service.registrar(solicitud));
        verify(solicitudPrestamoRepositoryPort, never()).guardar(any());
    }

    @Test
    void debeRegistrarLaSolicitudConEstadoPendienteRevision() {
        SolicitudPrestamo solicitud = crearSolicitudBase(new BigDecimal("1000000"), 12);
        when(usuarioRepositoryPort.existePorId(usuarioId)).thenReturn(true);
        when(tipoPrestamoRepositoryPort.existeActivoPorId(tipoPrestamoId)).thenReturn(true);
        when(solicitudPrestamoRepositoryPort.guardar(any(SolicitudPrestamo.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        SolicitudPrestamo resultado = service.registrar(solicitud);

        assertNotNull(resultado.getId());
        assertEquals(EstadoSolicitud.PENDIENTE_REVISION, resultado.getEstado());
        assertNotNull(resultado.getFechaSolicitud());
        assertNull(resultado.getFechaDecision());
        assertNull(resultado.getObservacionDecision());
        assertEquals(usuarioId, resultado.getUsuarioId());
        assertEquals(tipoPrestamoId, resultado.getTipoPrestamoId());
    }

    private SolicitudPrestamo crearSolicitudBase(BigDecimal monto, Integer plazoMeses) {
        return SolicitudPrestamo.builder()
            .usuarioId(usuarioId)
            .tipoPrestamoId(tipoPrestamoId)
            .monto(monto)
            .plazoMeses(plazoMeses)
            .build();
    }
}
