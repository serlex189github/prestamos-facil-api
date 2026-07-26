package com.prestamosfacil.domain.model;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
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
public class SolicitudPrestamo {

    private UUID id;

    private UUID usuarioId;

    private UUID tipoPrestamoId;

    private BigDecimal monto;

    private Integer plazoMeses;

    private EstadoSolicitud estado;

    private Instant fechaSolicitud;

    private Instant fechaDecision;

    private String observacionDecision;
}
