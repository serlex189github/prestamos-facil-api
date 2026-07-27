package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.CuotaPlanPagoRepositoryPort;
import com.prestamosfacil.domain.model.CuotaPlanPago;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.CuotaPlanPagoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.CuotaPlanPagoPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.CuotaPlanPagoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CuotaPlanPagoPersistenceAdapter
    implements CuotaPlanPagoRepositoryPort {

    private final CuotaPlanPagoJpaRepository cuotaPlanPagoJpaRepository;
    private final CuotaPlanPagoPersistenceMapper cuotaPlanPagoPersistenceMapper;

    @Override
    public List<CuotaPlanPago> guardarTodas(
        UUID prestamoId,
        List<CuotaPlanPago> cuotas
    ) {
        List<CuotaPlanPagoEntity> entities = cuotas.stream()
            .map(cuota ->
                cuotaPlanPagoPersistenceMapper.toEntity(
                    prestamoId,
                    cuota
                )
            )
            .toList();

        return cuotaPlanPagoJpaRepository
            .saveAll(entities)
            .stream()
            .map(cuotaPlanPagoPersistenceMapper::toDomain)
            .toList();
    }
}
