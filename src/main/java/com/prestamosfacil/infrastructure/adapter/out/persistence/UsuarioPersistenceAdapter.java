package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.domain.model.Usuario;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.UsuarioPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.UsuarioJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository usuarioJpaRepository;
    private final UsuarioPersistenceMapper usuarioPersistenceMapper;

    @Override
    public Usuario guardar(Usuario usuario) {
        UsuarioEntity entity = usuarioPersistenceMapper.toEntity(usuario);
        UsuarioEntity savedEntity = usuarioJpaRepository.save(entity);

        return usuarioPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return usuarioJpaRepository.existsByCorreoIgnoreCase(correo);
    }

    @Override
    public boolean existePorNumeroDocumento(String numeroDocumento) {
        return usuarioJpaRepository.existsByNumeroDocumento(numeroDocumento);
    }
}
