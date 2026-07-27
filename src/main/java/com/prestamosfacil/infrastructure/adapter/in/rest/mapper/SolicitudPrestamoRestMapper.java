package com.prestamosfacil.infrastructure.adapter.in.rest.mapper;

import com.prestamosfacil.domain.model.PaginaResultado;
import com.prestamosfacil.domain.model.SolicitudPrestamo;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.PaginaResponse;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.RegistrarSolicitudPrestamoRequest;
import com.prestamosfacil.infrastructure.adapter.in.rest.dto.SolicitudPrestamoResponse;
import org.springframework.stereotype.Component;

@Component
public class SolicitudPrestamoRestMapper {

    public SolicitudPrestamo toDomain(
        RegistrarSolicitudPrestamoRequest request
    ) {
        return SolicitudPrestamo.builder()
            .usuarioId(request.getUsuarioId())
            .tipoPrestamoId(request.getTipoPrestamoId())
            .monto(request.getMonto())
            .plazoMeses(request.getPlazoMeses())
            .build();
    }

    public SolicitudPrestamoResponse toResponse(
        SolicitudPrestamo solicitud
    ) {
        return SolicitudPrestamoResponse.builder()
            .id(solicitud.getId())
            .usuarioId(solicitud.getUsuarioId())
            .tipoPrestamoId(solicitud.getTipoPrestamoId())
            .monto(solicitud.getMonto())
            .plazoMeses(solicitud.getPlazoMeses())
            .estado(solicitud.getEstado())
            .fechaSolicitud(solicitud.getFechaSolicitud())
            .fechaDecision(solicitud.getFechaDecision())
            .observacionDecision(solicitud.getObservacionDecision())
            .build();
    }

    public PaginaResponse<SolicitudPrestamoResponse> toPageResponse(
        PaginaResultado<SolicitudPrestamo> pagina
    ) {
        return PaginaResponse.<SolicitudPrestamoResponse>builder()
            .contenido(
                pagina.getContenido()
                    .stream()
                    .map(this::toResponse)
                    .toList()
            )
            .pagina(pagina.getPagina())
            .tamano(pagina.getTamano())
            .totalElementos(pagina.getTotalElementos())
            .totalPaginas(pagina.getTotalPaginas())
            .primera(pagina.isPrimera())
            .ultima(pagina.isUltima())
            .build();
    }
}
