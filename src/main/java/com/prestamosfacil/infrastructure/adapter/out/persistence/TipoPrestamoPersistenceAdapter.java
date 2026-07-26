package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.TipoPrestamoRepositoryPort;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.TipoPrestamoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class TipoPrestamoPersistenceAdapter implements TipoPrestamoRepositoryPort {

    private final TipoPrestamoJpaRepository tipoPrestamoJpaRepository;

    @Override
    public boolean existeActivoPorId(UUID id) {
        return tipoPrestamoJpaRepository.existsByIdAndActivoTrue(id);
    }
}
