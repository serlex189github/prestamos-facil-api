package com.prestamosfacil.infrastructure.adapter.out.persistence.mapper;

import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.CuotaPlanPagoEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CuotaPlanPagoPersistenceMapper {

    public CuotaPlanPagoEntity toEntity(
        UUID prestamoId,
        CuotaPlanPago cuota
    ) {
        return CuotaPlanPagoEntity.builder()
            .prestamoId(prestamoId)
            .numeroCuota(cuota.getNumeroCuota())
            .fechaVencimiento(cuota.getFechaVencimiento())
            .saldoInicial(cuota.getSaldoInicial())
            .valorCuota(cuota.getValorCuota())
            .interes(cuota.getInteres())
            .abonoCapital(cuota.getAbonoCapital())
            .saldoFinal(cuota.getSaldoFinal())
            .build();
    }

    public CuotaPlanPago toDomain(CuotaPlanPagoEntity entity) {
        return CuotaPlanPago.builder()
            .numeroCuota(entity.getNumeroCuota())
            .fechaVencimiento(entity.getFechaVencimiento())
            .saldoInicial(entity.getSaldoInicial())
            .valorCuota(entity.getValorCuota())
            .interes(entity.getInteres())
            .abonoCapital(entity.getAbonoCapital())
            .saldoFinal(entity.getSaldoFinal())
            .build();
    }
}
