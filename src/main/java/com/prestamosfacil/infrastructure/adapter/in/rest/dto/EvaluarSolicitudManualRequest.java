package com.prestamosfacil.infrastructure.adapter.in.rest.dto;

import com.prestamosfacil.domain.enums.DecisionManual;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EvaluarSolicitudManualRequest(

    @NotNull(message = "La decisión es obligatoria")
    DecisionManual decision,

    @Size(
        max = 500,
        message = "La observación no puede superar los 500 caracteres"
    )
    String observacion
) {
}
