package com.prestamosfacil.infrastructure.config;

import com.prestamosfacil.application.port.in.RegistrarUsuarioUseCase;
import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.application.usecase.RegistrarUsuarioService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsuarioConfig {

    @Bean
    public RegistrarUsuarioUseCase registrarUsuarioUseCase(
        UsuarioRepositoryPort usuarioRepositoryPort
    ) {
        return new RegistrarUsuarioService(usuarioRepositoryPort);
    }
}
