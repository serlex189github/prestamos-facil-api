package com.prestamosfacil.application.port.out;

import com.prestamosfacil.domain.model.CuotaPlanPago;

import java.util.List;
import java.util.UUID;

public interface CuotaPlanPagoRepositoryPort {

    List<CuotaPlanPago> guardarTodas(
        UUID prestamoId,
        List<CuotaPlanPago> cuotas
    );
}
