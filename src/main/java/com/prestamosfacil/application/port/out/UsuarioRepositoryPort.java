package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.model.Usuario;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepositoryPort {

    Usuario guardar(Usuario usuario);

    boolean existePorCorreo(String correo);

    boolean existePorNumeroDocumento(String numeroDocumento);

    boolean existePorId(UUID id);

    Optional<Usuario> buscarPorId(UUID id);
}
