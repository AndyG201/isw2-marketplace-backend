CREATE TABLE public.solicitud_cambio_publicacion (
    id_solicitud uuid NOT NULL,
    id_publicacion uuid NOT NULL,
    id_anfitrion uuid NOT NULL,
    mensaje text NOT NULL,
    campos text NOT NULL,
    estado_anterior character varying(30) NOT NULL,
    fecha_solicitud timestamp with time zone NOT NULL,
    fecha_resolucion timestamp with time zone,
    CONSTRAINT solicitud_cambio_publicacion_pkey PRIMARY KEY (id_solicitud),
    CONSTRAINT solicitud_cambio_publicacion_publicacion_fkey FOREIGN KEY (id_publicacion)
        REFERENCES public.publicacion(id_publicacion)
);

CREATE INDEX idx_solicitud_cambio_publicacion_pendiente
    ON public.solicitud_cambio_publicacion (id_publicacion, fecha_solicitud DESC)
    WHERE fecha_resolucion IS NULL;
