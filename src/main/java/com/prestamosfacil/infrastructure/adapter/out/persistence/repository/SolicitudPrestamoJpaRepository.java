package com.prestamosfacil.infrastructure.adapter.out.persistence.repository;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.SolicitudPrestamoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SolicitudPrestamoJpaRepository
    extends JpaRepository<SolicitudPrestamoEntity, UUID> {

    Page<SolicitudPrestamoEntity> findByEstado(
        EstadoSolicitud estado,
        Pageable pageable
    );
}
