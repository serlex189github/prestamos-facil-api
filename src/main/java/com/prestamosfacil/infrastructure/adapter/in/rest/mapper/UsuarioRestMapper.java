package com.prestamosfacil.infrastructure.adapter.in.rest.mapper;

import com.prestamosfacil.domain.model.Usuario;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.RegistrarUsuarioRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import org.springframework.stereotype.Component;

@Component
public class UsuarioRestMapper {

    public Usuario toDomain(RegistrarUsuarioRequest request) {
        return Usuario.builder()
            .nombres(request.getNombres().trim())
            .apellidos(request.getApellidos().trim())
            .correo(request.getCorreo().trim().toLowerCase())
            .tipoDocumento(request.getTipoDocumento())
            .numeroDocumento(request.getNumeroDocumento().trim())
            .salarioBase(request.getSalarioBase())
            .build();
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
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
}
