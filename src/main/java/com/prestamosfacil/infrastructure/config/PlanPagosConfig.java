package com.prestamosfacil.infrastructure.config;

import com.prestamosfacil.application.port.in.GenerarPlanPagosUseCase;
import com.prestamosfacil.application.usecase.GenerarPlanPagosService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlanPagosConfig {

    @Bean
    public GenerarPlanPagosUseCase generarPlanPagosUseCase() {
        return new GenerarPlanPagosService();
    }
}
