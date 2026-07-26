package com.prestamosfacil.domain.model;

import com.prestamosfacil.domain.enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    private UUID id;

    private String nombres;

    private String apellidos;

    private String correo;

    private TipoDocumento tipoDocumento;

    private String numeroDocumento;

    private BigDecimal salarioBase;

    private Instant fechaCreacion;

}
