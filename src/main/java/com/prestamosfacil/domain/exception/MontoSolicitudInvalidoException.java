package com.prestamosfacil.domain.exception;

public class MontoSolicitudInvalidoException extends RuntimeException {

    public MontoSolicitudInvalidoException() {
        super("El monto de la solicitud debe ser mayor que cero.");
    }
}
