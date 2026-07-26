package com.prestamosfacil.application.port.in;

import com.prestamosfacil.domain.model.SolicitudPrestamo;

public interface RegistrarSolicitudPrestamoUseCase {

    SolicitudPrestamo registrar(SolicitudPrestamo solicitud);
}
