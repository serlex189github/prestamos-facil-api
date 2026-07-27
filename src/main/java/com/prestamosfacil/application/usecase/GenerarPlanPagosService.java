package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.domain.model.PlanPagos;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GenerarPlanPagosService implements GenerarPlanPagosUseCase {

    private static final int ESCALA_MONETARIA = 2;
    private static final int ESCALA_CALCULO = 16;
    private static final int PLAZO_MAXIMO = 72;

    private static final RoundingMode MODO_REDONDEO =
        RoundingMode.HALF_UP;

    private static final MathContext CONTEXTO_CALCULO =
        new MathContext(24, MODO_REDONDEO);

    private static final BigDecimal CIEN =
        BigDecimal.valueOf(100);

    private static final BigDecimal DOCE =
        BigDecimal.valueOf(12);

    private static final BigDecimal TASA_MAXIMA =
        BigDecimal.valueOf(100);

    @Override
    public PlanPagos generar(
        BigDecimal monto,
        BigDecimal tasaAnual,
        int plazoMeses,
        LocalDate fechaPrimeraCuota
    ) {
        validarParametros(
            monto,
            tasaAnual,
            plazoMeses,
            fechaPrimeraCuota
        );

        BigDecimal montoNormalizado = monto.setScale(
            ESCALA_MONETARIA,
            MODO_REDONDEO
        );

        BigDecimal tasaAnualNormalizada = tasaAnual.setScale(
            4,
            MODO_REDONDEO
        );

        BigDecimal tasaMensual = calcularTasaMensual(
            tasaAnualNormalizada
        );

        BigDecimal cuotaMensual = calcularCuotaMensual(
            montoNormalizado,
            tasaMensual,
            plazoMeses
        );

        List<CuotaPlanPago> cuotas = generarCuotas(
            montoNormalizado,
            tasaMensual,
            cuotaMensual,
            plazoMeses,
            fechaPrimeraCuota
        );

        BigDecimal totalIntereses = cuotas.stream()
            .map(CuotaPlanPago::getInteres)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(ESCALA_MONETARIA, MODO_REDONDEO);

        BigDecimal totalPagado = cuotas.stream()
            .map(CuotaPlanPago::getValorCuota)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(ESCALA_MONETARIA, MODO_REDONDEO);

        return PlanPagos.builder()
            .montoPrestamo(montoNormalizado)
            .tasaAnual(tasaAnualNormalizada)
            .tasaMensual(tasaMensual)
            .plazoMeses(plazoMeses)
            .cuotaMensual(cuotaMensual)
            .totalIntereses(totalIntereses)
            .totalPagado(totalPagado)
            .cuotas(List.copyOf(cuotas))
            .build();
    }

    private BigDecimal calcularTasaMensual(
        BigDecimal tasaAnual
    ) {
        /*
         * La tasa anual llega expresada como porcentaje.
         *
         * Ejemplo:
         * 18 % anual
         * 18 / 100 / 12 = 0.015 mensual
         */
        return tasaAnual
            .divide(
                CIEN,
                ESCALA_CALCULO,
                MODO_REDONDEO
            )
            .divide(
                DOCE,
                ESCALA_CALCULO,
                MODO_REDONDEO
            );
    }

    private BigDecimal calcularCuotaMensual(
        BigDecimal monto,
        BigDecimal tasaMensual,
        int plazoMeses
    ) {
        /*
         * Cuota = P × [i × (1 + i)^n]
         *              ----------------
         *                 (1 + i)^n - 1
         */

        BigDecimal factorPotencia = BigDecimal.ONE
            .add(tasaMensual)
            .pow(plazoMeses, CONTEXTO_CALCULO);

        BigDecimal numerador = monto
            .multiply(tasaMensual, CONTEXTO_CALCULO)
            .multiply(factorPotencia, CONTEXTO_CALCULO);

        BigDecimal denominador = factorPotencia
            .subtract(BigDecimal.ONE, CONTEXTO_CALCULO);

        return numerador
            .divide(
                denominador,
                ESCALA_CALCULO,
                MODO_REDONDEO
            )
            .setScale(
                ESCALA_MONETARIA,
                MODO_REDONDEO
            );
    }

    private List<CuotaPlanPago> generarCuotas(
        BigDecimal monto,
        BigDecimal tasaMensual,
        BigDecimal cuotaMensual,
        int plazoMeses,
        LocalDate fechaPrimeraCuota
    ) {
        List<CuotaPlanPago> cuotas = new ArrayList<>();

        BigDecimal saldoPendiente = monto;

        for (int numeroCuota = 1;
             numeroCuota <= plazoMeses;
             numeroCuota++) {

            BigDecimal saldoInicial = saldoPendiente;

            BigDecimal interes = saldoInicial
                .multiply(
                    tasaMensual,
                    CONTEXTO_CALCULO
                )
                .setScale(
                    ESCALA_MONETARIA,
                    MODO_REDONDEO
                );

            BigDecimal valorCuota = cuotaMensual;

            BigDecimal abonoCapital = valorCuota
                .subtract(interes)
                .setScale(
                    ESCALA_MONETARIA,
                    MODO_REDONDEO
                );

            /*
             * La última cuota se ajusta para eliminar cualquier
             * diferencia acumulada por redondeo.
             */
            if (numeroCuota == plazoMeses
                || abonoCapital.compareTo(saldoInicial) >= 0) {

                abonoCapital = saldoInicial.setScale(
                    ESCALA_MONETARIA,
                    MODO_REDONDEO
                );

                valorCuota = abonoCapital
                    .add(interes)
                    .setScale(
                        ESCALA_MONETARIA,
                        MODO_REDONDEO
                    );
            }

            BigDecimal saldoFinal = saldoInicial
                .subtract(abonoCapital)
                .setScale(
                    ESCALA_MONETARIA,
                    MODO_REDONDEO
                );

            if (saldoFinal.signum() < 0) {
                saldoFinal = BigDecimal.ZERO.setScale(
                    ESCALA_MONETARIA,
                    MODO_REDONDEO
                );
            }

            LocalDate fechaVencimiento = fechaPrimeraCuota
                .plusMonths(numeroCuota - 1L);

            cuotas.add(
                CuotaPlanPago.builder()
                    .numeroCuota(numeroCuota)
                    .fechaVencimiento(fechaVencimiento)
                    .saldoInicial(saldoInicial)
                    .valorCuota(valorCuota)
                    .interes(interes)
                    .abonoCapital(abonoCapital)
                    .saldoFinal(saldoFinal)
                    .build()
            );

            saldoPendiente = saldoFinal;
        }

        return cuotas;
    }

    private void validarParametros(
        BigDecimal monto,
        BigDecimal tasaAnual,
        int plazoMeses,
        LocalDate fechaPrimeraCuota
    ) {
        if (monto == null
            || monto.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                "El monto debe ser mayor que cero."
            );
        }

        if (tasaAnual == null
            || tasaAnual.compareTo(BigDecimal.ZERO) <= 0
            || tasaAnual.compareTo(TASA_MAXIMA) > 0) {

            throw new IllegalArgumentException(
                "La tasa anual debe ser mayor que cero "
                    + "y menor o igual que 100."
            );
        }

        if (plazoMeses < 1 || plazoMeses > PLAZO_MAXIMO) {
            throw new IllegalArgumentException(
                "El plazo debe estar entre 1 y 72 meses."
            );
        }

        if (fechaPrimeraCuota == null) {
            throw new IllegalArgumentException(
                "La fecha de la primera cuota es obligatoria."
            );
        }
    }
}
