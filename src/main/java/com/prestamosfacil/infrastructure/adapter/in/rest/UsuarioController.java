package com.prestamosfacil.infrastructure.adapter.in.rest;

import com.prestamosfacil.application.port.in.RegistrarUsuarioUseCase;
import com.prestamosfacil.domain.model.Usuario;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.RegistrarUsuarioRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.UsuarioResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.mapper.UsuarioRestMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final RegistrarUsuarioUseCase registrarUsuarioUseCase;
    private final UsuarioRestMapper usuarioRestMapper;

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(
        @Valid @RequestBody RegistrarUsuarioRequest request
    ) {
        Usuario usuario = usuarioRestMapper.toDomain(request);
        Usuario registrado = registrarUsuarioUseCase.registrar(usuario);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(usuarioRestMapper.toResponse(registrado));
    }
}
