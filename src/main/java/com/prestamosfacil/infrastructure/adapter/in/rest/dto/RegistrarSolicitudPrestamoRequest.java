package com.prestamosfacil.infrastructure.adapter.in.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarSolicitudPrestamoRequest {

    @NotNull(message = "El usuario es obligatorio")
    private UUID usuarioId;

    @NotNull(message = "El tipo de préstamo es obligatorio")
    private UUID tipoPrestamoId;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(
        value = "0.01",
        message = "El monto debe ser mayor que cero"
    )
    private BigDecimal monto;

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es de 1 mes")
    @Max(value = 72, message = "El plazo máximo es de 72 meses")
    private Integer plazoMeses;
}
