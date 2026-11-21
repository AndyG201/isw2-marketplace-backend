CREATE TABLE public.correo_pendiente (
    id_correo UUID PRIMARY KEY,
    destinatario VARCHAR(320) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    contenido_cifrado BYTEA NOT NULL,
    nonce BYTEA NOT NULL,
    clave_idempotencia VARCHAR(100) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    intentos INTEGER NOT NULL DEFAULT 0,
    proxima_ejecucion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lease_hasta TIMESTAMPTZ,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio TIMESTAMPTZ,
    ultimo_error TEXT,
    CONSTRAINT chk_correo_pendiente_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','ERROR','ENVIADO')),
    CONSTRAINT chk_correo_pendiente_intentos CHECK (intentos >= 0),
    CONSTRAINT chk_correo_pendiente_envio CHECK ((estado <> 'ENVIADO') OR fecha_envio IS NOT NULL)
);

CREATE INDEX idx_correo_pendiente_worker
    ON public.correo_pendiente (estado, proxima_ejecucion)
    WHERE estado IN ('PENDIENTE','ERROR','EN_PROCESO');
