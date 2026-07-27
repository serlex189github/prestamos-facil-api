package com.prestamosfacil.application.port.in;

import com.prestamosfacil.application.dto.ResultadoEvaluacionAutomatica;

import java.util.UUID;

public interface EvaluarSolicitudAutomaticaUseCase {

    ResultadoEvaluacionAutomatica evaluar(UUID solicitudId);

}
