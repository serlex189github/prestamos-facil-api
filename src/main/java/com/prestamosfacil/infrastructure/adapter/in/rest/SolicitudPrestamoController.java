package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.application.port.in.RegistrarSolicitudPrestamoUseCase;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.RegistrarSolicitudPrestamoRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.SolicitudPrestamoResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.mapper.SolicitudPrestamoRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/solicitudes")
@RequiredArgsConstructor
public class SolicitudPrestamoController {

    private final RegistrarSolicitudPrestamoUseCase registrarSolicitudPrestamoUseCase;
    private final SolicitudPrestamoRestMapper solicitudPrestamoRestMapper;

    @PostMapping
    public ResponseEntity<SolicitudPrestamoResponse> registrar(
        @Valid @RequestBody RegistrarSolicitudPrestamoRequest request
    ) {
        SolicitudPrestamo solicitud =
            solicitudPrestamoRestMapper.toDomain(request);

        SolicitudPrestamo registrada =
            registrarSolicitudPrestamoUseCase.registrar(solicitud);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(solicitudPrestamoRestMapper.toResponse(registrada));
    }
}
