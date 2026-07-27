package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.SolicitudPrestamoRepositoryPort;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.infrastructure.adapter.out.persistence.entity.SolicitudPrestamoEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.mapper.SolicitudPrestamoPersistenceMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.repository.SolicitudPrestamoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.prestamosfacil.domain.enums.EstadoSolicitud;
import com.prestamosfacil.domain.model.PaginaResultado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SolicitudPrestamoPersistenceAdapter
    implements SolicitudPrestamoRepositoryPort {

    private final SolicitudPrestamoJpaRepository solicitudPrestamoJpaRepository;
    private final SolicitudPrestamoPersistenceMapper solicitudPrestamoPersistenceMapper;

    @Override
    public Optional<SolicitudPrestamo> buscarPorId(UUID id) {
        return solicitudPrestamoJpaRepository
            .findById(id)
            .map(solicitudPrestamoPersistenceMapper::toDomain);
    }

    @Override
    public SolicitudPrestamo guardar(SolicitudPrestamo solicitud) {
        SolicitudPrestamoEntity entity =
            solicitudPrestamoPersistenceMapper.toEntity(solicitud);

        SolicitudPrestamoEntity savedEntity =
            solicitudPrestamoJpaRepository.save(entity);

        return solicitudPrestamoPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public PaginaResultado<SolicitudPrestamo> consultar(
        EstadoSolicitud estado,
        int pagina,
        int tamano
    ) {
        Pageable pageable = PageRequest.of(
            pagina,
            tamano,
            Sort.by(Sort.Direction.DESC, "fechaSolicitud")
        );

        Page<SolicitudPrestamoEntity> resultado;

        if (estado == null) {
            resultado = solicitudPrestamoJpaRepository.findAll(pageable);
        } else {
            resultado = solicitudPrestamoJpaRepository.findByEstado(
                estado,
                pageable
            );
        }

        List<SolicitudPrestamo> contenido = resultado
            .getContent()
            .stream()
            .map(solicitudPrestamoPersistenceMapper::toDomain)
            .toList();

        return PaginaResultado.<SolicitudPrestamo>builder()
            .contenido(contenido)
            .pagina(resultado.getNumber())
            .tamano(resultado.getSize())
            .totalElementos(resultado.getTotalElements())
            .totalPaginas(resultado.getTotalPages())
            .primera(resultado.isFirst())
            .ultima(resultado.isLast())
            .build();
    }
}
