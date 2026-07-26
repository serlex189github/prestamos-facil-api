package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.SolicitudPrestamoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.SolicitudPrestamoPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.SolicitudPrestamoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SolicitudPrestamoPersistenceAdapter
    implements SolicitudPrestamoRepositoryPort {

    private final SolicitudPrestamoJpaRepository solicitudPrestamoJpaRepository;
    private final SolicitudPrestamoPersistenceMapper solicitudPrestamoPersistenceMapper;

    @Override
    public SolicitudPrestamo guardar(SolicitudPrestamo solicitud) {
        SolicitudPrestamoEntity entity =
            solicitudPrestamoPersistenceMapper.toEntity(solicitud);

        SolicitudPrestamoEntity savedEntity =
            solicitudPrestamoJpaRepository.save(entity);

        return solicitudPrestamoPersistenceMapper.toDomain(savedEntity);
    }
}
