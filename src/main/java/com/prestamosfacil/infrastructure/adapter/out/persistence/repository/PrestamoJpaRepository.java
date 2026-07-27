package com.prestamosfacil.infrastructure.adapter.out.persistence.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.PrestamoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrestamoJpaRepository
    extends JpaRepository<PrestamoEntity, UUID> {
}
