package com.prestamosfacil.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class CuotaPlanPago {

    private Integer numeroCuota;
    private LocalDate fechaVencimiento;
    private BigDecimal saldoInicial;
    private BigDecimal valorCuota;
    private BigDecimal interes;
    private BigDecimal abonoCapital;
    private BigDecimal saldoFinal;
}
