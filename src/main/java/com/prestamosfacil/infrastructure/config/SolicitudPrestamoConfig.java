package com.prestamosfacil.infrastructure.config;

import com.prestamosfacil.application.port.in.*;
import com.prestamosfacil.application.port.out.*;
import com.prestamosfacil.application.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolicitudPrestamoConfig {

    @Bean
    public FormalizarPrestamoService formalizarPrestamoService(
        SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort,
        PrestamoRepositoryPort prestamoRepositoryPort,
        CuotaPlanPagoRepositoryPort cuotaPlanPagoRepositoryPort,
        GenerarPlanPagosUseCase generarPlanPagosUseCase
    ) {
        return new FormalizarPrestamoService(
            solicitudPrestamoRepositoryPort,
            prestamoRepositoryPort,
            cuotaPlanPagoRepositoryPort,
            generarPlanPagosUseCase
        );
    }

    @Bean
    public EvaluarSolicitudManualUseCase evaluarSolicitudManualUseCase(
        SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort,
        TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort,
        FormalizarPrestamoService formalizarPrestamoService,
        NotificacionPrestamoPort notificacionPrestamoPort
    ) {
        return new EvaluarSolicitudManualService(
            solicitudPrestamoRepositoryPort,
            tipoPrestamoRepositoryPort,
            notificacionPrestamoPort,
            formalizarPrestamoService
        );
    }

    @Bean
    public EvaluarSolicitudAutomaticaUseCase
    evaluarSolicitudAutomaticaUseCase(
        SolicitudPrestamoRepositoryPort solicitudPrestamoRepositoryPort,
        UsuarioRepositoryPort usuarioRepositoryPort,
        TipoPrestamoRepositoryPort tipoPrestamoRepositoryPort,
        PrestamoRepositoryPort prestamoRepositoryPort,
        EvaluacionAutomaticaRepositoryPort evaluacionAutomaticaRepositoryPort,
        GenerarPlanPagosUseCase generarPlanPagosUseCase,
        FormalizarPrestamoService formalizarPrestamoService,
        NotificacionPrestamoPort notificacionPrestamoPort
    ) {
        return new EvaluarSolicitudAutomaticaService(
            solicitudPrestamoRepositoryPort,
            usuarioRepositoryPort,
            tipoPrestamoRepositoryPort,
            prestamoRepositoryPort,
            evaluacionAutomaticaRepositoryPort,
            generarPlanPagosUseCase,
            formalizarPrestamoService,
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
