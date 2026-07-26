package com.prestamosfacil.infrastructure.adapter.out.persistence.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.SolicitudPrestamoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SolicitudPrestamoJpaRepository
    extends JpaRepository<SolicitudPrestamoEntity, UUID> {
}
