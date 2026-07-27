package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.model.Prestamo;

import java.math.BigDecimal;
import java.util.UUID;

public interface PrestamoRepositoryPort {

    Prestamo guardar(Prestamo prestamo);

    BigDecimal obtenerDeudaMensualActiva(UUID usuarioId);
}
