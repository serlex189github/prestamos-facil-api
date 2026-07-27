package com.prestamosfacil.application.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ResultadoFormalizacionPrestamo {

    private UUID solicitudId;
    private UUID prestamoId;
}
