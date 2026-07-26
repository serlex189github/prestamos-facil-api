CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE usuario (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(254) NOT NULL,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(30) NOT NULL,
    salario_base NUMERIC(15, 2) NOT NULL,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT uk_usuario_numero_documento UNIQUE (numero_documento),

    CONSTRAINT ck_usuario_nombres_no_vacios
        CHECK (BTRIM(nombres) <> ''),

    CONSTRAINT ck_usuario_apellidos_no_vacios
        CHECK (BTRIM(apellidos) <> ''),

    CONSTRAINT ck_usuario_correo_no_vacio
        CHECK (BTRIM(correo) <> ''),

    CONSTRAINT ck_usuario_numero_documento_no_vacio
        CHECK (BTRIM(numero_documento) <> ''),

    CONSTRAINT ck_usuario_tipo_documento
        CHECK (tipo_documento IN ('CC', 'CE', 'PASAPORTE', 'NIT')),

    CONSTRAINT ck_usuario_salario_base
        CHECK (salario_base >= 0 AND salario_base <= 15000000)
);

CREATE TABLE tipo_prestamo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    tasa_anual NUMERIC(8, 4) NOT NULL,
    validacion_automatica BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_tipo_prestamo_nombre UNIQUE (nombre),

    CONSTRAINT ck_tipo_prestamo_nombre_no_vacio
        CHECK (BTRIM(nombre) <> ''),

    CONSTRAINT ck_tipo_prestamo_tasa_anual
        CHECK (tasa_anual > 0 AND tasa_anual <= 100)
);

CREATE TABLE solicitud_prestamo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    tipo_prestamo_id UUID NOT NULL,
    monto NUMERIC(15, 2) NOT NULL,
    plazo_meses INTEGER NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE_REVISION',
    fecha_solicitud TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_decision TIMESTAMP WITH TIME ZONE,
    observacion_decision VARCHAR(500),

    CONSTRAINT fk_solicitud_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario (id),

    CONSTRAINT fk_solicitud_tipo_prestamo
        FOREIGN KEY (tipo_prestamo_id)
        REFERENCES tipo_prestamo (id),

    CONSTRAINT ck_solicitud_monto
        CHECK (monto > 0),

    CONSTRAINT ck_solicitud_plazo
        CHECK (plazo_meses BETWEEN 1 AND 72),

    CONSTRAINT ck_solicitud_estado
        CHECK (
            estado IN (
                'PENDIENTE_REVISION',
                'REVISION_MANUAL',
                'APROBADA',
                'RECHAZADA'
            )
        )
);

CREATE TABLE prestamo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    solicitud_id UUID NOT NULL,
    monto_original NUMERIC(15, 2) NOT NULL,
    saldo_pendiente NUMERIC(15, 2) NOT NULL,
    tasa_anual NUMERIC(8, 4) NOT NULL,
    plazo_meses INTEGER NOT NULL,
    cuota_mensual NUMERIC(15, 2) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    fecha_aprobacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_primer_pago DATE NOT NULL,

    CONSTRAINT uk_prestamo_solicitud UNIQUE (solicitud_id),

    CONSTRAINT fk_prestamo_solicitud
        FOREIGN KEY (solicitud_id)
        REFERENCES solicitud_prestamo (id),

    CONSTRAINT ck_prestamo_monto_original
        CHECK (monto_original > 0),

    CONSTRAINT ck_prestamo_saldo_pendiente
        CHECK (saldo_pendiente >= 0),

    CONSTRAINT ck_prestamo_tasa_anual
        CHECK (tasa_anual > 0 AND tasa_anual <= 100),

    CONSTRAINT ck_prestamo_plazo
        CHECK (plazo_meses BETWEEN 1 AND 72),

    CONSTRAINT ck_prestamo_cuota_mensual
        CHECK (cuota_mensual > 0),

    CONSTRAINT ck_prestamo_estado
        CHECK (estado IN ('ACTIVO', 'PAGADO', 'CANCELADO'))
);

CREATE TABLE cuota_plan_pago (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prestamo_id UUID NOT NULL,
    numero_cuota INTEGER NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    saldo_inicial NUMERIC(15, 2) NOT NULL,
    valor_cuota NUMERIC(15, 2) NOT NULL,
    interes NUMERIC(15, 2) NOT NULL,
    abono_capital NUMERIC(15, 2) NOT NULL,
    saldo_final NUMERIC(15, 2) NOT NULL,

    CONSTRAINT fk_cuota_plan_pago_prestamo
        FOREIGN KEY (prestamo_id)
        REFERENCES prestamo (id)
        ON DELETE CASCADE,

    CONSTRAINT uk_cuota_prestamo_numero
        UNIQUE (prestamo_id, numero_cuota),

    CONSTRAINT ck_cuota_numero
        CHECK (numero_cuota > 0),

    CONSTRAINT ck_cuota_saldo_inicial
        CHECK (saldo_inicial >= 0),

    CONSTRAINT ck_cuota_valor
        CHECK (valor_cuota > 0),

    CONSTRAINT ck_cuota_interes
        CHECK (interes >= 0),

    CONSTRAINT ck_cuota_abono_capital
        CHECK (abono_capital >= 0),

    CONSTRAINT ck_cuota_saldo_final
        CHECK (saldo_final >= 0)
);

CREATE INDEX idx_solicitud_usuario
    ON solicitud_prestamo (usuario_id);

CREATE INDEX idx_solicitud_tipo_prestamo
    ON solicitud_prestamo (tipo_prestamo_id);

CREATE INDEX idx_solicitud_estado
    ON solicitud_prestamo (estado);

CREATE INDEX idx_solicitud_fecha
    ON solicitud_prestamo (fecha_solicitud DESC);

CREATE INDEX idx_prestamo_estado
    ON prestamo (estado);

CREATE INDEX idx_cuota_prestamo
    ON cuota_plan_pago (prestamo_id);

CREATE INDEX idx_cuota_fecha_vencimiento
    ON cuota_plan_pago (fecha_vencimiento);
