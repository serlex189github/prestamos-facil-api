package com.prestamosfacil.application.dto;

import com.prestamosfacil.domain.enums.EstadoPrestamo;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Value
@Builder
public class DetallePrestamoAprobado {

    UUID prestamoId;
    UUID solicitudId;
    BigDecimal montoAprobado;
    EstadoPrestamo estado;
    Instant fechaAprobacion;
}
