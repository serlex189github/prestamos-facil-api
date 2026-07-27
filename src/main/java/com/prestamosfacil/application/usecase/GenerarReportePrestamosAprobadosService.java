package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.dto.DetallePrestamoAprobado;
import com.prestamosfacil.application.dto.ReportePrestamosAprobados;
import com.prestamosfacil.application.port.in.GenerarReportePrestamosAprobadosUseCase;
import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoPrestamo;
import com.prestamosfacil.domain.model.Prestamo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenerarReportePrestamosAprobadosService
    implements GenerarReportePrestamosAprobadosUseCase {

    private static final List<EstadoPrestamo> ESTADOS_APROBADOS =
        List.of(
            EstadoPrestamo.ACTIVO,
            EstadoPrestamo.PAGADO
        );

    private final PrestamoRepositoryPort prestamoRepositoryPort;

    @Override
    @Transactional(readOnly = true)
    public ReportePrestamosAprobados generar() {

        log.info("Generando reporte de préstamos aprobados");

        List<Prestamo> prestamos =
            prestamoRepositoryPort.buscarPorEstados(
                ESTADOS_APROBADOS
            );

        List<DetallePrestamoAprobado> detalles = prestamos.stream()
            .map(this::mapearDetalle)
            .toList();

        BigDecimal montoTotal = detalles.stream()
            .map(DetallePrestamoAprobado::getMontoAprobado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        ReportePrestamosAprobados reporte =
            ReportePrestamosAprobados.builder()
                .montoTotalAprobado(montoTotal)
                .cantidadPrestamos(detalles.size())
                .prestamos(detalles)
                .build();

        log.info(
            "Reporte generado. cantidadPrestamos={}, montoTotalAprobado={}",
            reporte.getCantidadPrestamos(),
            reporte.getMontoTotalAprobado()
        );

        return reporte;
    }

    private DetallePrestamoAprobado mapearDetalle(
        Prestamo prestamo
    ) {
        return DetallePrestamoAprobado.builder()
            .prestamoId(prestamo.getId())
            .solicitudId(prestamo.getSolicitudId())
            .montoAprobado(prestamo.getMontoOriginal())
            .estado(prestamo.getEstado())
            .fechaAprobacion(prestamo.getFechaAprobacion())
            .build();
    }
}
