package com.prestamosfacil.infrastructure.adapter.out.persistence.mapper;

import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.SolicitudPrestamoEntity;
import org.springframework.stereotype.Component;

@Component
public class SolicitudPrestamoPersistenceMapper {

    public SolicitudPrestamoEntity toEntity(SolicitudPrestamo solicitud) {
        return SolicitudPrestamoEntity.builder()
            .id(solicitud.getId())
            .usuarioId(solicitud.getUsuarioId())
            .tipoPrestamoId(solicitud.getTipoPrestamoId())
            .monto(solicitud.getMonto())
            .plazoMeses(solicitud.getPlazoMeses())
            .estado(solicitud.getEstado())
            .fechaSolicitud(solicitud.getFechaSolicitud())
            .fechaDecision(solicitud.getFechaDecision())
            .observacionDecision(solicitud.getObservacionDecision())
            .build();
    }

    public SolicitudPrestamo toDomain(SolicitudPrestamoEntity entity) {
        return SolicitudPrestamo.builder()
            .id(entity.getId())
            .usuarioId(entity.getUsuarioId())
            .tipoPrestamoId(entity.getTipoPrestamoId())
            .monto(entity.getMonto())
            .plazoMeses(entity.getPlazoMeses())
            .estado(entity.getEstado())
            .fechaSolicitud(entity.getFechaSolicitud())
            .fechaDecision(entity.getFechaDecision())
            .observacionDecision(entity.getObservacionDecision())
            .build();
    }
}
