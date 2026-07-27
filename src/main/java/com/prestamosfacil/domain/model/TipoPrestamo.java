package com.prestamosfacil.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoPrestamo {

    private UUID id;
    private String nombre;
    private BigDecimal tasaAnual;
    private Boolean validacionAutomatica;
    private Boolean activo;
    private Instant fechaCreacion;
}
