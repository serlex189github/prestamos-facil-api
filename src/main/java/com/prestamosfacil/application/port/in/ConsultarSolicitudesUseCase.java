package com.prestamosfacil.application.port.in;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PaginaResultado;
import com.prestamosfacil.domain.model.SolicitudPrestamo;

public interface ConsultarSolicitudesUseCase {

    PaginaResultado<SolicitudPrestamo> consultar(
        EstadoSolicitud estado,
        int pagina,
        int tamano
    );
}
