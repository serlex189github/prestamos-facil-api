package com.prestamosfacil.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cuota_plan_pago")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuotaPlanPagoEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "prestamo_id", nullable = false)
    private UUID prestamoId;

    @Column(name = "numero_cuota", nullable = false)
    private Integer numeroCuota;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    @Column(
        name = "saldo_inicial",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal saldoInicial;

    @Column(
        name = "valor_cuota",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal valorCuota;

    @Column(
        name = "interes",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal interes;

    @Column(
        name = "abono_capital",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal abonoCapital;

    @Column(
        name = "saldo_final",
        nullable = false,
        precision = 15,
        scale = 2
    )
    private BigDecimal saldoFinal;
}
