package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.model.Prestamo;

public interface PrestamoRepositoryPort {

    Prestamo guardar(Prestamo prestamo);
}
