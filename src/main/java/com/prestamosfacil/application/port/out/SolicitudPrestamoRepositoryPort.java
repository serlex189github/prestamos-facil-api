package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.model.SolicitudPrestamo;

public interface SolicitudPrestamoRepositoryPort {

    SolicitudPrestamo guardar(SolicitudPrestamo solicitud);
}
