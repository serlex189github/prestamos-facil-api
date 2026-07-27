package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PaginaResultado;
import com.prestamosfacil.domain.model.SolicitudPrestamo;

import java.util.Optional;
import java.util.UUID;

public interface SolicitudPrestamoRepositoryPort {

    SolicitudPrestamo guardar(SolicitudPrestamo solicitud);

    Optional<SolicitudPrestamo> buscarPorId(UUID id);

    PaginaResultado<SolicitudPrestamo> consultar(
        EstadoSolicitud estado,
        int pagina,
        int tamano
    );
}
