package com.prestamosfacil.infrastructure.config;

import com.prestamosfacil.application.port.in.ConsultarSolicitudesUseCase;
import com.prestamosfacil.application.port.in.EvaluarSolicitudManualUseCase;
import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.port.in.RegistrarSolicitudPrestamoUseCase;
import com.prestamosfacil.application.port.out.*;
import com.prestamosfacil.application.usecase.ConsultarSolicitudesService;
import com.prestamosfacil.application.usecase.EvaluarSolicitudManualService;
import com.prestamosfacil.application.usecase.RegistrarSolicitudPrestamoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolicitudPrestamoConfig {

    @Bean
    public EvaluarSolicitudManualUseCase evaluarSolicitudManualUseCase(
        SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort,
        TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort,
        PrestamoRepositoryPort prestamoRepositoryPort,
        CuotaPlanPagoRepositoryPort cuotaPlanPagoRepositoryPort,
        GenerarPlanPagosUseCase generarPlanPagosUseCase,
        NotificacionPrestamoPort notificacionPrestamoPort
    ) {
        return new EvaluarSolicitudManualService(
            solicitudPrestamoRepositoryPort,
            tipoPrestamoRepositoryPort,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            generarPlanPagosUseCase,
            notificacionPrestamoPort
        );
    }

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

    @Bean
    public ConsultarSolicitudesUseCase consultarSolicitudesUseCase(
        SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort
    ) {
        return new ConsultarSolicitudesService(
            solicitudPrestamoRepositoryPort
        );
    }
}
