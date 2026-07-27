package com.prestamosfacil.infrastructure.adapter.out.persistence.mapper;

import com.prestamosfacil.domain.model.TipoPrestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.TipoPrestamoEntity;
import org.springframework.stereotype.Component;

@Component
public class TipoPrestamoPersistenceMapper {

    public TipoPrestamoEntity toEntity(TipoPrestamo tipoPrestamo) {
        return TipoPrestamoEntity.builder()
            .id(tipoPrestamo.getId())
            .nombre(tipoPrestamo.getNombre())
            .tasaAnual(tipoPrestamo.getTasaAnual())
            .validacionAutomatica(tipoPrestamo.getValidacionAutomatica())
            .activo(tipoPrestamo.getActivo())
            .fechaCreacion(tipoPrestamo.getFechaCreacion())
            .build();
    }

    public TipoPrestamo toDomain(TipoPrestamoEntity entity) {
        return TipoPrestamo.builder()
            .id(entity.getId())
            .nombre(entity.getNombre())
            .tasaAnual(entity.getTasaAnual())
            .validacionAutomatica(entity.getValidacionAutomatica())
            .activo(entity.getActivo())
            .fechaCreacion(entity.getFechaCreacion())
            .build();
    }
}
