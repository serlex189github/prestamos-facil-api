package com.prestamosfacil.infrastructure.adapter.out.persistence.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.PrestamoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface PrestamoJpaRepository
    extends JpaRepository<PrestamoEntity, UUID> {

    @Query("""
        SELECT COALESCE(SUM(p.cuotaMensual), 0)
        FROM PrestamoEntity p
        JOIN SolicitudPrestamoEntity s
            ON s.id = p.solicitudId
        WHERE s.usuarioId = :usuarioId
          AND p.estado = com.prestamosfacil.domain.enums.EstadoPrestamo.ACTIVO
        """)
    BigDecimal obtenerDeudaMensualActiva(
        @Param("usuarioId") UUID usuarioId
    );

}
