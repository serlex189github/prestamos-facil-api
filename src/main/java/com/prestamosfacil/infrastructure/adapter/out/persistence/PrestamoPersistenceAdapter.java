package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.PrestamoRepositoryPort;
import com.prestamosfacil.domain.model.Prestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.PrestamoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.PrestamoPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.PrestamoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

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

    @Override
    public BigDecimal obtenerDeudaMensualActiva(UUID usuarioId) {
        if (usuarioId == null) {
            throw new IllegalArgumentException(
                "El identificador del usuario es obligatorio"
            );
        }

        BigDecimal deudaMensual =
            prestamoJpaRepository.obtenerDeudaMensualActiva(usuarioId);

        BigDecimal resultado = deudaMensual != null
            ? deudaMensual
            : BigDecimal.ZERO;

        log.debug(
            "Deuda mensual activa calculada. usuarioId={}, deudaMensual={}",
            usuarioId,
            resultado
        );

        return resultado;
    }
}
