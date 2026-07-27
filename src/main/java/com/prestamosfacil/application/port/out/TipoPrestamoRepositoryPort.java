package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.model.TipoPrestamo;

import java.util.Optional;
import java.util.UUID;

public interface TipoPrestamoRepositoryPort {

    boolean existeActivoPorId(UUID id);

    Optional<TipoPrestamo> buscarPorId(UUID id);
}
