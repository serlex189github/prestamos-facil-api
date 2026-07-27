package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.enums.EstadoSolicitud;

import java.math.BigDecimal;

public interface EvaluacionAutomaticaRepositoryPort {

    EstadoSolicitud evaluar(
        BigDecimal salarioBase,
        BigDecimal deudaMensualActual,
        BigDecimal cuotaNueva,
        BigDecimal montoSolicitado
    );
}
