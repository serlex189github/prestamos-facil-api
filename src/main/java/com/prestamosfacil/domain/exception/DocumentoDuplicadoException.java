package com.prestamosfacil.domain.exception;

public class DocumentoDuplicadoException extends RuntimeException {

    public DocumentoDuplicadoException(String numeroDocumento) {
        super("Ya existe un usuario registrado con el documento: " + numeroDocumento);
    }

}
