package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.model.Usuario;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    boolean existePorCorreo(String correo);

    boolean existePorNumeroDocumento(String numeroDocumento);
}
