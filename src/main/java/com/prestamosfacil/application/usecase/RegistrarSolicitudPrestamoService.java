package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.in.RegistrarSolicitudPrestamoUseCase;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.exception.MontoSolicitudInvalidoException;
import com.prestamosfacil.domain.exception.PlazoSolicitudInvalidoException;
import com.prestamosfacil.domain.exception.TipoPrestamoNoDisponibleException;
import com.prestamosfacil.domain.exception.UsuarioNoEncontradoException;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class RegistrarSolicitudPrestamoService
    implements RegistrarSolicitudPrestamoUseCase {

    private static final int PLAZO_MINIMO = 1;
    private static final int PLAZO_MAXIMO = 72;

    private final SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort;

    @Override
    public SolicitudPrestamo registrar(SolicitudPrestamo solicitud) {
        validarSolicitud(solicitud);

        if (!usuarioRepositoryPort.existePorId(solicitud.getUsuarioId())) {
            throw new UsuarioNoEncontradoException(solicitud.getUsuarioId());
        }

        if (!tipoPrestamoRepositoryPort.existeActivoPorId(
            solicitud.getTipoPrestamoId()
        )) {
            throw new TipoPrestamoNoDisponibleException(
                solicitud.getTipoPrestamoId()
            );
        }

        SolicitudPrestamo nuevaSolicitud = SolicitudPrestamo.builder()
            .id(UUID.randomUUID())
            .usuarioId(solicitud.getUsuarioId())
            .tipoPrestamoId(solicitud.getTipoPrestamoId())
            .monto(solicitud.getMonto())
            .plazoMeses(solicitud.getPlazoMeses())
            .estado(EstadoSolicitud.PENDIENTE_REVISION)
            .fechaSolicitud(Instant.now())
            .fechaDecision(null)
            .observacionDecision(null)
            .build();

        return solicitudPrestamoRepositoryPort.guardar(nuevaSolicitud);
    }

    private void validarSolicitud(SolicitudPrestamo solicitud) {
        if (solicitud == null) {
            throw new IllegalArgumentException(
                "La solicitud de préstamo es obligatoria."
            );
        }

        BigDecimal monto = solicitud.getMonto();

        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontoSolicitudInvalidoException();
        }

        Integer plazo = solicitud.getPlazoMeses();

        if (plazo == null
            || plazo < PLAZO_MINIMO
            || plazo > PLAZO_MAXIMO) {
            throw new PlazoSolicitudInvalidoException();
        }

        if (solicitud.getUsuarioId() == null) {
            throw new UsuarioNoEncontradoException(null);
        }

        if (solicitud.getTipoPrestamoId() == null) {
            throw new TipoPrestamoNoDisponibleException(null);
        }
    }
}
