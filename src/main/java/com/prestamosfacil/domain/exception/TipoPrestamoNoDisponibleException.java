package com.prestamosfacil.domain.exception;

import java.util.UUID;

public class TipoPrestamoNoDisponibleException extends RuntimeException {

    public TipoPrestamoNoDisponibleException(UUID tipoPrestamoId) {
        super(
            "El tipo de préstamo no existe o no se encuentra activo: "
                + tipoPrestamoId
        );
    }
}
