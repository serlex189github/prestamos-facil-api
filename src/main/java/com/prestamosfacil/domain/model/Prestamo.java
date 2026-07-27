package com.prestamosfacil.domain.model;

import com.prestamosfacil.domain.enums.EstadoPrestamo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prestamo {

    private UUID id;
    private UUID solicitudId;
    private BigDecimal montoOriginal;
    private BigDecimal saldoPendiente;
    private BigDecimal tasaAnual;
    private Integer plazoMeses;
    private BigDecimal cuotaMensual;
    private EstadoPrestamo estado;
    private Instant fechaAprobacion;
    private LocalDate fechaPrimerPago;
}
