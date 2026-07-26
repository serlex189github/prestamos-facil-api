package com.prestamosfacil.application.usecase;

import com.prestamosfacil.application.port.in.RegistrarUsuarioUseCase;
import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.exception.CorreoDuplicadoException;
import com.prestamosfacil.domain.exception.DocumentoDuplicadoException;
import com.prestamosfacil.domain.exception.SalarioInvalidoException;
import com.prestamosfacil.domain.model.Usuario;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class RegistrarUsuarioService implements RegistrarUsuarioUseCase {

    private static final BigDecimal SALARIO_MINIMO = BigDecimal.ZERO;
    private static final BigDecimal SALARIO_MAXIMO = new BigDecimal("15000000");

    private final UsuarioRepositoryPort usuarioRepositoryPort;

    @Override
    public Usuario registrar(Usuario usuario) {

        validarSalario(usuario.getSalarioBase());

        if (usuarioRepositoryPort.existePorCorreo(usuario.getCorreo())) {
            throw new CorreoDuplicadoException(usuario.getCorreo());
        }

        if (usuarioRepositoryPort.existePorNumeroDocumento(usuario.getNumeroDocumento())) {
            throw new DocumentoDuplicadoException(usuario.getNumeroDocumento());
        }

        Usuario nuevoUsuario = Usuario.builder()
            .id(UUID.randomUUID())
            .nombres(usuario.getNombres())
            .apellidos(usuario.getApellidos())
            .correo(usuario.getCorreo())
            .tipoDocumento(usuario.getTipoDocumento())
            .numeroDocumento(usuario.getNumeroDocumento())
            .salarioBase(usuario.getSalarioBase())
            .fechaCreacion(Instant.now())
            .build();

        return usuarioRepositoryPort.guardar(nuevoUsuario);
    }

    private void validarSalario(BigDecimal salario) {

        if (salario.compareTo(SALARIO_MINIMO) < 0
            || salario.compareTo(SALARIO_MAXIMO) > 0) {
            throw new SalarioInvalidoException();
        }

    }

}
