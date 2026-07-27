package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.application.dto.ResultadoEvaluacionAutomatica;
import com.prestamosfacil.application.port.in.EvaluarSolicitudAutomaticaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class EvaluacionAutomaticaController {

    private final EvaluarSolicitudAutomaticaUseCase
        evaluarSolicitudAutomaticaUseCase;

    @Operation(summary = "Ejecuta la evaluación automática de una solicitud de préstamo")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Evaluación realizada correctamente"),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
        @ApiResponse(responseCode = "404", description = "Solicitud no encontrada")
    })
    @PostMapping("/{solicitudId}/evaluacion-automatica")
    public ResponseEntity<ResultadoEvaluacionAutomatica> evaluar(
        @PathVariable UUID solicitudId
    ) {
        ResultadoEvaluacionAutomatica resultado =
            evaluarSolicitudAutomaticaUseCase.evaluar(solicitudId);

        return ResponseEntity.ok(resultado);
    }
}
