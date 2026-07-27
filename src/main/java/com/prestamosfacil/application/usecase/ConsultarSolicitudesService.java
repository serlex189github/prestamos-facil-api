package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.in.ConsultarSolicitudesUseCase;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PaginaResultado;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConsultarSolicitudesService
    implements ConsultarSolicitudesUseCase {

    private static final int TAMANO_MAXIMO = 100;

    private final SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;

    @Override
    public PaginaResultado<SolicitudPrestamo> consultar(
        EstadoSolicitud estado,
        int pagina,
        int tamano
    ) {
        if (pagina < 0) {
            throw new IllegalArgumentException(
                "El número de página no puede ser negativo."
            );
        }

        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                "El tamaño de página debe estar entre 1 y 100."
            );
        }

        return solicitudPrestamoRepositoryPort.consultar(
            estado,
            pagina,
            tamano
        );
    }
}
