package com.prestamosfacil.infrastructure.adapter.in.rest.dto;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudPrestamoResponse {

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
