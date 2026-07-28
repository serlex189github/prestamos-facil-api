package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.dto.ReportePrestamosAprobados;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoPrestamo;
import com.prestamosfacil.domain.model.Prestamo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerarReportePrestamosAprobadosServiceTest {

    @Mock
    private PrestamoRepositoryPort prestamoRepositoryPort;

    private GenerarReportePrestamosAprobadosService service;

    @BeforeEach
    void setUp() {
        service = new GenerarReportePrestamosAprobadosService(prestamoRepositoryPort);
    }

    @Test
    void debeConsultarSoloPrestamosActivosOPagados() {
        when(prestamoRepositoryPort.buscarPorEstados(List.of(
            EstadoPrestamo.ACTIVO, EstadoPrestamo.PAGADO
        ))).thenReturn(List.of());

        service.generar();

        ArgumentCaptor<List<EstadoPrestamo>> captor = ArgumentCaptor.forClass(List.class);
        verify(prestamoRepositoryPort).buscarPorEstados(captor.capture());
        assertEquals(List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.PAGADO), captor.getValue());
    }

    @Test
    void debeRetornarReporteVacioCuandoNoHayPrestamos() {
        when(prestamoRepositoryPort.buscarPorEstados(anyList())).thenReturn(List.of());

        ReportePrestamosAprobados reporte = service.generar();

        assertEquals(0, reporte.getCantidadPrestamos());
        assertEquals(0, reporte.getMontoTotalAprobado().compareTo(BigDecimal.ZERO));
        assertTrue(reporte.getPrestamos().isEmpty());
    }

    @Test
    void debeSumarElMontoTotalYMapearCadaPrestamo() {
        Prestamo prestamo1 = crearPrestamo(new BigDecimal("1000000.00"), EstadoPrestamo.ACTIVO);
        Prestamo prestamo2 = crearPrestamo(new BigDecimal("2500000.00"), EstadoPrestamo.PAGADO);

        when(prestamoRepositoryPort.buscarPorEstados(anyList()))
            .thenReturn(List.of(prestamo1, prestamo2));

        ReportePrestamosAprobados reporte = service.generar();

        assertEquals(2, reporte.getCantidadPrestamos());
        assertEquals(0, reporte.getMontoTotalAprobado().compareTo(new BigDecimal("3500000.00")));

        assertEquals(prestamo1.getId(), reporte.getPrestamos().get(0).getPrestamoId());
        assertEquals(prestamo1.getSolicitudId(), reporte.getPrestamos().get(0).getSolicitudId());
        assertEquals(prestamo1.getMontoOriginal(), reporte.getPrestamos().get(0).getMontoAprobado());
    }

    private Prestamo crearPrestamo(BigDecimal monto, EstadoPrestamo estado) {
        return Prestamo.builder()
            .id(UUID.randomUUID())
            .solicitudId(UUID.randomUUID())
            .montoOriginal(monto)
            .saldoPendiente(monto)
            .estado(estado)
            .fechaAprobacion(Instant.now())
            .build();
    }

    @SuppressWarnings("unchecked")
    private static List<EstadoPrestamo> anyList() {
        return org.mockito.ArgumentMatchers.anyList();
    }
}
