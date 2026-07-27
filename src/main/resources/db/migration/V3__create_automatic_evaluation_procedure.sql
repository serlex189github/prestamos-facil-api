CREATE OR REPLACE PROCEDURE evaluar_solicitud_automatica(
    IN p_salario_base NUMERIC,
    IN p_deuda_mensual_actual NUMERIC,
    IN p_cuota_nueva NUMERIC,
    IN p_monto_solicitado NUMERIC,
    INOUT p_decision VARCHAR(30)
)
LANGUAGE plpgsql
AS
$$
DECLARE
    v_capacidad_maxima NUMERIC;
BEGIN
    IF p_salario_base IS NULL OR p_salario_base <= 0 THEN
        RAISE EXCEPTION 'El salario base debe ser mayor que cero';
    END IF;

    IF p_deuda_mensual_actual IS NULL OR p_deuda_mensual_actual < 0 THEN
        RAISE EXCEPTION
            'La deuda mensual actual no puede ser negativa';
    END IF;

    IF p_cuota_nueva IS NULL OR p_cuota_nueva <= 0 THEN
        RAISE EXCEPTION 'La cuota nueva debe ser mayor que cero';
    END IF;

    IF p_monto_solicitado IS NULL OR p_monto_solicitado <= 0 THEN
        RAISE EXCEPTION
            'El monto solicitado debe ser mayor que cero';
    END IF;

    v_capacidad_maxima := p_salario_base * 0.35;

    IF (p_deuda_mensual_actual + p_cuota_nueva)
        > v_capacidad_maxima THEN

        p_decision := 'RECHAZADA';

    ELSIF p_monto_solicitado > (p_salario_base * 5) THEN

        p_decision := 'REVISION_MANUAL';

    ELSE

        p_decision := 'APROBADA';

    END IF;
END;
$$;
