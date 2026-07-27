package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.application.dto.ReportePrestamosAprobados;
import com.prestamosfacil.application.port.in.GenerarReportePrestamosAprobadosUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReportePrestamosController {

    private final GenerarReportePrestamosAprobadosUseCase
        generarReportePrestamosAprobadosUseCase;

    @GetMapping("/prestamos-aprobados")
    public ResponseEntity<ReportePrestamosAprobados>
    generarReportePrestamosAprobados() {

        ReportePrestamosAprobados reporte =
            generarReportePrestamosAprobadosUseCase.generar();

        return ResponseEntity.ok(reporte);
    }
}
