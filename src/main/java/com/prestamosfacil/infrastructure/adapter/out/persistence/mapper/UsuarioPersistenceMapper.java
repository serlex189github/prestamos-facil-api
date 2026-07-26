package com.prestamosfacil.infrastructure.adapter.out.persistence.mapper;

import com.prestamosfacil.domain.model.Usuario;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.UsuarioEntity;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPersistenceMapper {

    public UsuarioEntity toEntity(Usuario usuario) {
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

    public Usuario toDomain(UsuarioEntity entity) {
        return Usuario.builder()
            .id(entity.getId())
            .nombres(entity.getNombres())
            .apellidos(entity.getApellidos())
            .correo(entity.getCorreo())
            .tipoDocumento(entity.getTipoDocumento())
            .numeroDocumento(entity.getNumeroDocumento())
            .salarioBase(entity.getSalarioBase())
            .fechaCreacion(entity.getFechaCreacion())
            .build();
    }
}
