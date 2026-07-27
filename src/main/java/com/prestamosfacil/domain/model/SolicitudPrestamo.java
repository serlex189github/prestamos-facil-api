package com.prestamosfacil.domain.model;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
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
