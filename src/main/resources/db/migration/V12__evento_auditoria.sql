-- A6: indice de eventos de auditoria archivados en Backblaze B2.
-- V1 crea public.bandeja_salida_auditoria (outbox); esta tabla es distinta.
-- Corresponde a AuditoriaIndiceJdbcRepository y AuditoriaConsultaJdbcRepository.
-- No modificar V1...V11 ya aplicadas por Flyway.

CREATE TABLE public.evento_auditoria (
    id_evento UUID PRIMARY KEY,
    id_usuario UUID,
    fecha_evento TIMESTAMPTZ NOT NULL,
    accion VARCHAR(100) NOT NULL,
    resultado VARCHAR(30) NOT NULL,
    entidad_afectada VARCHAR(50) NOT NULL,
    id_entidad_afectada UUID,
    id_correlacion VARCHAR(100),
    clave_objeto TEXT NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    modo_retencion VARCHAR(20) NOT NULL,
    retencion_hasta TIMESTAMPTZ NOT NULL,
    version_objeto TEXT,
    CONSTRAINT chk_evento_auditoria_sha256
        CHECK (sha256 ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_evento_auditoria_modo_retencion
        CHECK (modo_retencion = 'COMPLIANCE')
);

-- Pagina de auditoria: ORDER BY fecha_evento DESC, id_evento DESC.
CREATE INDEX idx_evento_auditoria_fecha
    ON public.evento_auditoria (fecha_evento DESC, id_evento DESC);

-- Filtro por id_usuario combinado con el intervalo de fechas.
CREATE INDEX idx_evento_auditoria_usuario_fecha
    ON public.evento_auditoria (id_usuario, fecha_evento DESC, id_evento DESC)
    WHERE id_usuario IS NOT NULL;

-- Filtro habitual por accion y fecha de evento.
CREATE INDEX idx_evento_auditoria_accion_fecha
    ON public.evento_auditoria (accion, fecha_evento DESC, id_evento DESC);

COMMENT ON TABLE public.evento_auditoria IS
    'Indice de metadatos de eventos de auditoria almacenados con retencion COMPLIANCE en B2';
