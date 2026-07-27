package com.prestamosfacil.application.port.in;

import com.prestamosfacil.domain.enums.DecisionManual;
import com.prestamosfacil.domain.model.ResultadoEvaluacionManual;

import java.util.UUID;

public interface EvaluarSolicitudManualUseCase {

    ResultadoEvaluacionManual evaluar(
        UUID solicitudId,
        DecisionManual decision,
        String observacion
    );
}
