package com.prestamosfacil.application.usecase;

import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.domain.model.PlanPagos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GenerarPlanPagosServiceTest {

    private GenerarPlanPagosService service;
    private LocalDate fechaPrimeraCuota;

    @BeforeEach
    void setUp() {
        service = new GenerarPlanPagosService();
        fechaPrimeraCuota = LocalDate.now().plusMonths(1);
    }

    // ---------- VALIDACIÓN DE PARÁMETROS ----------

    @Test
    void debeFallarCuandoMontoEsNulo() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(null, new BigDecimal("12"), 12, fechaPrimeraCuota)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-100"})
    void debeFallarCuandoMontoNoEsPositivo(String monto) {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(new BigDecimal(monto), new BigDecimal("12"), 12, fechaPrimeraCuota)
        );
    }

    @Test
    void debeFallarCuandoTasaAnualEsNula() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(new BigDecimal("1000000"), null, 12, fechaPrimeraCuota)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "100.01", "150"})
    void debeFallarCuandoTasaAnualEstaFueraDeRango(String tasa) {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(new BigDecimal("1000000"), new BigDecimal(tasa), 12, fechaPrimeraCuota)
        );
    }

    @Test
    void debePermitirTasaAnualDeExactamente100() {
        // Límite superior inclusivo: no debe lanzar excepción.
        PlanPagos plan = service.generar(
            new BigDecimal("1000000"), new BigDecimal("100"), 6, fechaPrimeraCuota
        );
        assertEquals(new BigDecimal("100.0000"), plan.getTasaAnual());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 73, 100})
    void debeFallarCuandoPlazoEstaFueraDeRango(int plazo) {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(new BigDecimal("1000000"), new BigDecimal("12"), plazo, fechaPrimeraCuota)
        );
    }

    @Test
    void debeFallarCuandoFechaPrimeraCuotaEsNula() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.generar(new BigDecimal("1000000"), new BigDecimal("12"), 12, null)
        );
    }

    // ---------- CÁLCULO DEL PLAN ----------

    @Test
    void debeGenerarUnaCuotaPorCadaMesDelPlazo() {
        PlanPagos plan = service.generar(
            new BigDecimal("5000000"), new BigDecimal("18"), 24, fechaPrimeraCuota
        );

        assertEquals(24, plan.getCuotas().size());
    }

    @Test
    void elSaldoDeLaUltimaCuotaDebeQuedarEnCero() {
        PlanPagos plan = service.generar(
            new BigDecimal("3000000"), new BigDecimal("15"), 12, fechaPrimeraCuota
        );

        CuotaPlanPago ultimaCuota = plan.getCuotas().get(plan.getCuotas().size() - 1);
        assertEquals(0, ultimaCuota.getSaldoFinal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void laSumaDeAbonosACapitalDebeSerIgualAlMontoPrestado() {
        BigDecimal monto = new BigDecimal("10000000.00");
        PlanPagos plan = service.generar(monto, new BigDecimal("20"), 36, fechaPrimeraCuota);

        BigDecimal sumaAbonos = plan.getCuotas().stream()
            .map(CuotaPlanPago::getAbonoCapital)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tolerancia de 1 centavo por acumulación de redondeo entre cuotas.
        assertTrue(sumaAbonos.subtract(monto).abs().compareTo(new BigDecimal("0.01")) <= 0);
    }

    @Test
    void elInteresDebeDisminuirAMedidaQueAvanzanLasCuotas() {
        PlanPagos plan = service.generar(
            new BigDecimal("8000000"), new BigDecimal("16"), 18, fechaPrimeraCuota
        );

        BigDecimal interesPrimeraCuota = plan.getCuotas().get(0).getInteres();
        BigDecimal interesUltimaCuota = plan.getCuotas().get(plan.getCuotas().size() - 1).getInteres();

        assertTrue(interesPrimeraCuota.compareTo(interesUltimaCuota) > 0);
    }

    @Test
    void laPrimeraCuotaDebeVencerEnLaFechaIndicada() {
        PlanPagos plan = service.generar(
            new BigDecimal("2000000"), new BigDecimal("10"), 6, fechaPrimeraCuota
        );

        assertEquals(fechaPrimeraCuota, plan.getCuotas().get(0).getFechaVencimiento());
        assertEquals(
            fechaPrimeraCuota.plusMonths(5),
            plan.getCuotas().get(5).getFechaVencimiento()
        );
    }

    @Test
    void elTotalPagadoDebeSerLaSumaDeTodasLasCuotas() {
        PlanPagos plan = service.generar(
            new BigDecimal("4000000"), new BigDecimal("22"), 12, fechaPrimeraCuota
        );

        BigDecimal sumaCuotas = plan.getCuotas().stream()
            .map(CuotaPlanPago::getValorCuota)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, java.math.RoundingMode.HALF_UP);

        assertEquals(0, sumaCuotas.compareTo(plan.getTotalPagado()));
    }

    @Test
    void debeGenerarUnaSolaCuotaCuandoElPlazoEsUnMes() {
        PlanPagos plan = service.generar(
            new BigDecimal("1000000"), new BigDecimal("12"), 1, fechaPrimeraCuota
        );

        assertEquals(1, plan.getCuotas().size());
        CuotaPlanPago unicaCuota = plan.getCuotas().get(0);
        assertEquals(0, unicaCuota.getSaldoFinal().compareTo(BigDecimal.ZERO));
        assertEquals(0, unicaCuota.getAbonoCapital().compareTo(new BigDecimal("1000000.00")));
    }
}
