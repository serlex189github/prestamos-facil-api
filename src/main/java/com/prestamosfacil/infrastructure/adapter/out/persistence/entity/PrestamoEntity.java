package com.prestamosfacil.infrastructure.adapter.out.persistence.entity;

import com.prestamosfacil.domain.enums.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Table(name = "prestamo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrestamoEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "solicitud_id", nullable = false, unique = true)
    private UUID solicitudId;

    @Column(name = "monto_original", nullable = false, precision = 15, scale = 2)
    private BigDecimal montoOriginal;

    @Column(name = "saldo_pendiente", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoPendiente;

    @Column(name = "tasa_anual", nullable = false, precision = 8, scale = 4)
    private BigDecimal tasaAnual;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Column(name = "cuota_mensual", nullable = false, precision = 15, scale = 2)
    private BigDecimal cuotaMensual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPrestamo estado;

    @Column(name = "fecha_aprobacion", nullable = false)
    private Instant fechaAprobacion;

    @Column(name = "fecha_primer_pago", nullable = false)
    private LocalDate fechaPrimerPago;
}
