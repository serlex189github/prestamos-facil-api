package com.prestamosfacil.domain.exception;

public class PlazoSolicitudInvalidoException extends RuntimeException {

    public PlazoSolicitudInvalidoException() {
        super("El plazo debe estar entre 1 y 72 meses.");
    }
}
