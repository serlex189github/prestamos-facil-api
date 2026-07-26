package com.prestamosfacil.infrastructure.adapter.out.persistence.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.TipoPrestamoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TipoPrestamoJpaRepository
    extends JpaRepository<TipoPrestamoEntity, UUID> {

    boolean existsByIdAndActivoTrue(UUID id);
}
