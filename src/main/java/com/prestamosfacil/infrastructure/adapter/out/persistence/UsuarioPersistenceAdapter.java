package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.model.Usuario;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entidad = toEntity(usuario);
        UsuarioEntity guardado = usuarioJpaRepository.save(entidad);
        return toDomain(guardado);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return usuarioJpaRepository.existsByCorreoIgnoreCase(correo);
    }

    @Override
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        return usuarioJpaRepository.existsByNumeroDocumento(numeroDocumento);
    }

    private UsuarioEntity toEntity(Usuario usuario) {
        return UsuarioEntity.builder()
            .id(usuario.getId())
            .nombres(usuario.getNombres())
            .apellidos(usuario.getApellidos())
            .correo(usuario.getCorreo())
            .tipoDocumento(usuario.getTipoDocumento())
            .numeroDocumento(usuario.getNumeroDocumento())
            .salarioBase(usuario.getSalarioBase())
            .fechaCreacion(usuario.getFechaCreacion())
            .build();
    }

    private Usuario toDomain(UsuarioEntity entidad) {
        return Usuario.builder()
            .id(entidad.getId())
            .nombres(entidad.getNombres())
            .apellidos(entidad.getApellidos())
            .correo(entidad.getCorreo())
            .tipoDocumento(entidad.getTipoDocumento())
            .numeroDocumento(entidad.getNumeroDocumento())
            .salarioBase(entidad.getSalarioBase())
            .fechaCreacion(entidad.getFechaCreacion())
            .build();
    }
}
