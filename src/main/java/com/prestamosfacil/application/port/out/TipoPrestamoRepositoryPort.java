package com.prestamosfacil.application.port.out;

import java.util.UUID;

public interface TipoPrestamoRepositoryPort {

    boolean existeActivoPorId(UUID id);
}
