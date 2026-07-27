package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.PrestamoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.PrestamoPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.PrestamoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PrestamoPersistenceAdapter
    implements PrestamoRepositoryPort {

    private final PrestamoJpaRepository prestamoJpaRepository;
    private final PrestamoPersistenceMapper prestamoPersistenceMapper;

    @Override
    public Prestamo guardar(Prestamo prestamo) {

        PrestamoEntity entity =
            prestamoPersistenceMapper.toEntity(prestamo);

        PrestamoEntity savedEntity =
            prestamoJpaRepository.save(entity);

        return prestamoPersistenceMapper.toDomain(savedEntity);
    }
}
