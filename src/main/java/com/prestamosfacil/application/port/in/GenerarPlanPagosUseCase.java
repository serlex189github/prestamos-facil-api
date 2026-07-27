package com.prestamosfacil.application.port.in;

import com.prestamosfacil.domain.model.PlanPagos;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface GenerarPlanPagosUseCase {

    PlanPagos generar(
        BigDecimal monto,
        BigDecimal tasaAnual,
        int plazoMeses,
        LocalDate fechaPrimeraCuota
    );
}
