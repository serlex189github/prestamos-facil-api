package com.prestamosfacil.infrastructure.adapter.out.persistence.mapper;

import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.PrestamoEntity;
import org.springframework.stereotype.Component;

@Component
public class PrestamoPersistenceMapper {

    public PrestamoEntity toEntity(Prestamo prestamo) {
        return PrestamoEntity.builder()
            .id(prestamo.getId())
            .solicitudId(prestamo.getSolicitudId())
            .montoOriginal(prestamo.getMontoOriginal())
            .saldoPendiente(prestamo.getSaldoPendiente())
            .tasaAnual(prestamo.getTasaAnual())
            .plazoMeses(prestamo.getPlazoMeses())
            .cuotaMensual(prestamo.getCuotaMensual())
            .estado(prestamo.getEstado())
            .fechaAprobacion(prestamo.getFechaAprobacion())
            .fechaPrimerPago(prestamo.getFechaPrimerPago())
            .build();
    }

    public Prestamo toDomain(PrestamoEntity entity) {
        return Prestamo.builder()
            .id(entity.getId())
            .solicitudId(entity.getSolicitudId())
            .montoOriginal(entity.getMontoOriginal())
            .saldoPendiente(entity.getSaldoPendiente())
            .tasaAnual(entity.getTasaAnual())
            .plazoMeses(entity.getPlazoMeses())
            .cuotaMensual(entity.getCuotaMensual())
            .estado(entity.getEstado())
            .fechaAprobacion(entity.getFechaAprobacion())
            .fechaPrimerPago(entity.getFechaPrimerPago())
            .build();
    }
}
