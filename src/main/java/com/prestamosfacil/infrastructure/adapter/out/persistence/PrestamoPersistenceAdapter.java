package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.PrestamoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.PrestamoPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.PrestamoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PrestamoPersistenceAdapter
    implements PrestamoRepositoryPort {

    private final PrestamoJpaRepository prestamoJpaRepository;
    private final PrestamoPersistenceMapper prestamoPersistenceMapper;

    @Override
    public Prestamo guardar(Prestamo prestamo) {

        log.debug(
            "Guardando préstamo nuevo. id={}, solicitudId={}",
            prestamo.getId(),
            prestamo.getSolicitudId()
        );

        PrestamoEntity entity =
            prestamoPersistenceMapper.toEntity(prestamo);

        log.debug(
            "Entidad préstamo antes de persistir. id={}, solicitudId={}",
            entity.getId(),
            entity.getSolicitudId()
        );

        PrestamoEntity savedEntity =
            prestamoJpaRepository.save(entity);


        log.debug(
            "Préstamo guardado correctamente. id={}, solicitudId={}",
            savedEntity.getId(),
            savedEntity.getSolicitudId()
        );
        return prestamoPersistenceMapper.toDomain(savedEntity);
    }
}
