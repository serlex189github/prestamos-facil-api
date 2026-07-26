package com.prestamosfacil.infrastructure.config;

import com.prestamosfacil.application.port.in.RegistrarSolicitudPrestamoUseCase;
import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.application.port.out.UsuarioRepositoryPort;
import com.prestamosfacil.application.usecase.RegistrarSolicitudPrestamoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolicitudPrestamoConfig {

    @Bean
    public RegistrarSolicitudPrestamoUseCase registrarSolicitudPrestamoUseCase(
        SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort,
        UsuarioRepositoryPort usuarioRepositoryPort,
        TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort
    ) {
        return new RegistrarSolicitudPrestamoService(
            solicitudPrestamoRepositoryPort,
            usuarioRepositoryPort,
            tipoPrestamoRepositoryPort
        );
    }
}
