package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.domain.model.TipoPrestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.TipoPrestamoPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.TipoPrestamoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TipoPrestamoPersistenceAdapter implements TipoPrestamoRepositoryPort {

    private final TipoPrestamoJpaRepository tipoPrestamoJpaRepository;
    private final TipoPrestamoPersistenceMapper tipoPrestamoPersistenceMapper;

    @Override
    public Optional<TipoPrestamo> buscarPorId(UUID id) {
        return tipoPrestamoJpaRepository
            .findById(id)
            .map(tipoPrestamoPersistenceMapper::toDomain);
    }

    @Override
    public boolean existeActivoPorId(UUID id) {
        return tipoPrestamoJpaRepository.existsByIdAndActivoTrue(id);
    }
}
