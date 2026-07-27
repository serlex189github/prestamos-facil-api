package com.prestamosfacil.infrastructure.adapter.in.rest.mapper;

import com.prestamosfacil.domain.model.ResultadoEvaluacionManual;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.EvaluarSolicitudManualResponse;
import org.springframework.stereotype.Component;

@Component
public class EvaluacionManualRestMapper {

    public EvaluarSolicitudManualResponse toResponse(
        ResultadoEvaluacionManual resultado
    ) {
        return new EvaluarSolicitudManualResponse(
            resultado.getSolicitudId(),
            resultado.getEstado(),
            resultado.getPrestamoId(),
            resultado.getMensaje()
        );
    }
}
