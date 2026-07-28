package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PaginaResultado;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarSolicitudesServiceTest {

    @Mock
    private SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;

    private ConsultarSolicitudesService service;

    @BeforeEach
    void setUp() {
        service = new ConsultarSolicitudesService(solicitudPrestamoRepositoryPort);
    }

    @Test
    void debeFallarCuandoPaginaEsNegativa() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.consultar(EstadoSolicitud.PENDIENTE_REVISION, -1, 10)
        );
        verifyNoInteractions(solicitudPrestamoRepositoryPort);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -50, 101, 500})
    void debeFallarCuandoTamanoEstaFueraDeRango(int tamano) {
        if (tamano == 0 || tamano < 1 || tamano > 100) {
            assertThrows(
                IllegalArgumentException.class,
                () -> service.consultar(EstadoSolicitud.PENDIENTE_REVISION, 0, tamano)
            );
        }
        verifyNoInteractions(solicitudPrestamoRepositoryPort);
    }

}
