package com.prestamosfacil.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PlanPagos {

    private BigDecimal montoPrestamo;
    private BigDecimal tasaAnual;
    private BigDecimal tasaMensual;
    private Integer plazoMeses;
    private BigDecimal cuotaMensual;
    private BigDecimal totalIntereses;
    private BigDecimal totalPagado;
    private List<CuotaPlanPago> cuotas;
}
