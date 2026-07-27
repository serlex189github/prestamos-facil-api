package com.prestamosfacil.infrastructure.adapter.out.persistence;

import com.prestamosfacil.application.port.out.EvaluacionAutomaticaRepositoryPort;
import com.prestamosfacil.domain.enums.EstadoSolicitud;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Types;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EvaluacionAutomaticaPersistenceAdapter
    implements EvaluacionAutomaticaRepositoryPort {

    private static final String PROCEDURE_CALL =
        "{call evaluar_solicitud_automatica(?, ?, ?, ?, ?)}";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public EstadoSolicitud evaluar(
        BigDecimal salarioBase,
        BigDecimal deudaMensualActual,
        BigDecimal cuotaNueva,
        BigDecimal montoSolicitado
    ) {
        log.debug(
            "Ejecutando evaluación automática. "
                + "salarioBase={}, deudaMensualActual={}, "
                + "cuotaNueva={}, montoSolicitado={}",
            salarioBase,
            deudaMensualActual,
            cuotaNueva,
            montoSolicitado
        );

        CallableStatementCreator statementCreator = connection -> {
            CallableStatement statement =
                connection.prepareCall(PROCEDURE_CALL);

            statement.setBigDecimal(1, salarioBase);
            statement.setBigDecimal(2, deudaMensualActual);
            statement.setBigDecimal(3, cuotaNueva);
            statement.setBigDecimal(4, montoSolicitado);

            statement.registerOutParameter(5, Types.VARCHAR);
            statement.setNull(5, Types.VARCHAR);

            return statement;
        };

        CallableStatementCallback<String> statementCallback =
            callableStatement -> {
                callableStatement.execute();
                return callableStatement.getString(5);
            };

        String decision = jdbcTemplate.execute(
            statementCreator,
            statementCallback
        );

        if (decision == null || decision.isBlank()) {
            throw new IllegalStateException(
                "El procedimiento de evaluación automática "
                    + "no devolvió una decisión"
            );
        }

        try {
            EstadoSolicitud resultado = EstadoSolicitud.valueOf(
                decision.trim().toUpperCase()
            );

            log.debug(
                "Resultado de evaluación automática: {}",
                resultado
            );

            return resultado;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                "El procedimiento devolvió una decisión "
                    + "no reconocida: " + decision,
                exception
            );
        }
    }
}
