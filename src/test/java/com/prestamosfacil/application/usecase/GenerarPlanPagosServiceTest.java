package com.prestamosfacil.application.usecase;

import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.domain.model.PlanPagos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GenerarPlanPagosServiceTest {

    private GenerarPlanPagosService service;

    @BeforeEach
    void setUp() {
        service = new GenerarPlanPagosService();
    }

    @Test
    void deberiaGenerarPlanPagosCompleto() {
        BigDecimal monto = new BigDecimal("5000000");
        BigDecimal tasaAnual = new BigDecimal("18");
        int plazoMeses = 24;
        LocalDate fechaPrimeraCuota = LocalDate.of(2026, 8, 27);

        PlanPagos plan = service.generar(
            monto,
            tasaAnual,
            plazoMeses,
            fechaPrimeraCuota
        );

        assertNotNull(plan);
        assertNotNull(plan.getCuotas());

        assertEquals(plazoMeses, plan.getCuotas().size());
        assertEquals(
            0,
            plan.getMontoPrestamo()
                .compareTo(new BigDecimal("5000000.00"))
        );

        assertEquals(
            0,
            plan.getTasaAnual()
                .compareTo(new BigDecimal("18.0000"))
        );

        assertEquals(
            0,
            plan.getTasaMensual()
                .compareTo(new BigDecimal("0.0150000000000000"))
        );
    }

    @Test
    void deberiaGenerarPrimeraCuotaConSaldoInicialIgualAlMonto() {
        PlanPagos plan = service.generar(
            new BigDecimal("5000000"),
            new BigDecimal("18"),
            24,
            LocalDate.of(2026, 8, 27)
        );

        CuotaPlanPago primeraCuota = plan.getCuotas().getFirst();

        assertEquals(1, primeraCuota.getNumeroCuota());

        assertEquals(
            0,
            primeraCuota.getSaldoInicial()
                .compareTo(new BigDecimal("5000000.00"))
        );

        assertEquals(
            LocalDate.of(2026, 8, 27),
            primeraCuota.getFechaVencimiento()
        );
    }

    @Test
    void deberiaDejarSaldoFinalEnCeroEnLaUltimaCuota() {
        PlanPagos plan = service.generar(
            new BigDecimal("5000000"),
            new BigDecimal("18"),
            24,
            LocalDate.of(2026, 8, 27)
        );

        CuotaPlanPago ultimaCuota = plan
            .getCuotas()
            .getLast();

        assertEquals(24, ultimaCuota.getNumeroCuota());

        assertEquals(
            0,
            ultimaCuota.getSaldoFinal()
                .compareTo(new BigDecimal("0.00"))
        );
    }

    @Test
    void sumaDeAbonosCapitalDeberiaSerIgualAlMontoPrestado() {
        BigDecimal monto = new BigDecimal("5000000");

        PlanPagos plan = service.generar(
            monto,
            new BigDecimal("18"),
            24,
            LocalDate.of(2026, 8, 27)
        );

        BigDecimal totalCapital = plan.getCuotas()
            .stream()
            .map(CuotaPlanPago::getAbonoCapital)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(
            0,
            totalCapital.compareTo(new BigDecimal("5000000.00"))
        );
    }

    @Test
    void totalPagadoDeberiaSerCapitalMasIntereses() {
        PlanPagos plan = service.generar(
            new BigDecimal("5000000"),
            new BigDecimal("18"),
            24,
            LocalDate.of(2026, 8, 27)
        );

        BigDecimal esperado = plan.getMontoPrestamo()
            .add(plan.getTotalIntereses());

        assertEquals(
            0,
            esperado.compareTo(plan.getTotalPagado())
        );
    }

    @Test
    void deberiaGenerarFechasDeVencimientoMesAMes() {
        LocalDate fechaInicial = LocalDate.of(2026, 8, 27);

        PlanPagos plan = service.generar(
            new BigDecimal("5000000"),
            new BigDecimal("18"),
            3,
            fechaInicial
        );

        assertEquals(
            LocalDate.of(2026, 8, 27),
            plan.getCuotas().get(0).getFechaVencimiento()
        );

        assertEquals(
            LocalDate.of(2026, 9, 27),
            plan.getCuotas().get(1).getFechaVencimiento()
        );

        assertEquals(
            LocalDate.of(2026, 10, 27),
            plan.getCuotas().get(2).getFechaVencimiento()
        );
    }

    @Test
    void deberiaAjustarLaUltimaCuotaPorRedondeo() {
        PlanPagos plan = service.generar(
            new BigDecimal("1000000"),
            new BigDecimal("17.5"),
            7,
            LocalDate.of(2026, 8, 27)
        );

        CuotaPlanPago ultimaCuota = plan
            .getCuotas()
            .getLast();

        assertEquals(
            0,
            ultimaCuota.getSaldoFinal()
                .compareTo(new BigDecimal("0.00"))
        );

        assertEquals(
            0,
            ultimaCuota.getValorCuota().compareTo(
                ultimaCuota.getAbonoCapital()
                    .add(ultimaCuota.getInteres())
            )
        );
    }

    @Test
    void deberiaRechazarMontoNulo() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(
                null,
                new BigDecimal("18"),
                24,
                LocalDate.of(2026, 8, 27)
            )
        );

        assertEquals(
            "El monto debe ser mayor que cero.",
            exception.getMessage()
        );
    }

    @Test
    void deberiaRechazarMontoIgualACero() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(
                BigDecimal.ZERO,
                new BigDecimal("18"),
                24,
                LocalDate.of(2026, 8, 27)
            )
        );

        assertEquals(
            "El monto debe ser mayor que cero.",
            exception.getMessage()
        );
    }

    @Test
    void deberiaRechazarTasaMayorQueCien() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(
                new BigDecimal("5000000"),
                new BigDecimal("100.01"),
                24,
                LocalDate.of(2026, 8, 27)
            )
        );

        assertEquals(
            "La tasa anual debe ser mayor que cero y menor o igual que 100.",
            exception.getMessage()
        );
    }

    @Test
    void deberiaRechazarPlazoMayorQueSetentaYDosMeses() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(
                new BigDecimal("5000000"),
                new BigDecimal("18"),
                73,
                LocalDate.of(2026, 8, 27)
            )
        );

        assertEquals(
            "El plazo debe estar entre 1 y 72 meses.",
            exception.getMessage()
        );
    }

    @Test
    void deberiaRechazarFechaPrimeraCuotaNula() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(
                new BigDecimal("5000000"),
                new BigDecimal("18"),
                24,
                null
            )
        );

        assertEquals(
            "La fecha de la primera cuota es obligatoria.",
            exception.getMessage()
        );
    }
}
