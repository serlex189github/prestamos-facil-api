package com.prestamosfacil.domain.exception;

import java.util.UUID;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(UUID usuarioId) {
        super("No existe un usuario registrado con id: " + usuarioId);
    }
}
