CREATE TABLE public.reembolso_pago (
    id_reembolso UUID PRIMARY KEY,
    id_pago UUID NOT NULL UNIQUE REFERENCES public.pago(id_pago),
    id_reserva UUID NOT NULL REFERENCES public.reserva(id_reserva),
    payment_intent_id VARCHAR(255) NOT NULL,
    stripe_refund_id VARCHAR(255),
    clave_idempotencia VARCHAR(100) NOT NULL UNIQUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    intentos INTEGER NOT NULL DEFAULT 0,
    proxima_ejecucion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_ejecucion TIMESTAMPTZ,
    ultimo_error TEXT,
    fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reembolso_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'REEMBOLSADO', 'FALLIDA')),
    CONSTRAINT chk_reembolso_intentos CHECK (intentos >= 0)
);

CREATE INDEX idx_reembolso_worker
    ON public.reembolso_pago (estado, proxima_ejecucion)
    WHERE estado IN ('PENDIENTE', 'EN_PROCESO');
