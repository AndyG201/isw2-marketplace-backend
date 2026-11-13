--
-- PostgreSQL database dump
--

-- Dumped from database version 18.6
-- Dumped by pg_dump version 18.6

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: alojamiento; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.alojamiento (
    id_alojamiento uuid NOT NULL,
    id_anfitrion uuid NOT NULL,
    ubicacion character varying(255) NOT NULL,
    ciudad character varying(100) NOT NULL,
    tipo_alojamiento character varying(50) NOT NULL,
    capacidad integer NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_alojamiento_capacidad CHECK ((capacidad > 0))
);


--
-- Name: bandeja_salida_auditoria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.bandeja_salida_auditoria (
    id_evento uuid NOT NULL,
    id_usuario uuid,
    fecha_evento timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    accion character varying(100) NOT NULL,
    resultado character varying(30) NOT NULL,
    entidad_afectada character varying(50) NOT NULL,
    id_entidad_afectada uuid,
    detalle jsonb,
    id_correlacion character varying(100),
    estado_envio character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    intentos_envio integer DEFAULT 0 NOT NULL,
    proxima_ejecucion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_envio timestamp with time zone,
    ultimo_error text,
    CONSTRAINT chk_auditoria_envio CHECK ((((estado_envio)::text <> 'ENVIADO'::text) OR (fecha_envio IS NOT NULL))),
    CONSTRAINT chk_auditoria_estado CHECK (((estado_envio)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'EN_PROCESO'::character varying, 'ENVIADO'::character varying, 'ERROR'::character varying])::text[]))),
    CONSTRAINT chk_auditoria_intentos CHECK ((intentos_envio >= 0))
);


--
-- Name: cancelacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cancelacion (
    id_cancelacion uuid NOT NULL,
    id_reserva uuid NOT NULL,
    id_usuario uuid NOT NULL,
    fecha_cancelacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    motivo text NOT NULL,
    valor_devolucion numeric(12,2) DEFAULT 0 NOT NULL,
    estado_devolucion character varying(30) NOT NULL,
    estado_final character varying(30) NOT NULL,
    CONSTRAINT chk_cancelacion_devolucion CHECK ((valor_devolucion >= (0)::numeric)),
    CONSTRAINT chk_cancelacion_estado CHECK (((estado_devolucion)::text = ANY ((ARRAY['NO_APLICA'::character varying, 'PENDIENTE'::character varying, 'PROCESADA'::character varying, 'FALLIDA'::character varying])::text[])))
);


--
-- Name: configuracion_tarifa; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.configuracion_tarifa (
    id_configuracion uuid NOT NULL,
    id_administrador uuid NOT NULL,
    porcentaje_servicio numeric(5,2) NOT NULL,
    porcentaje_descuento numeric(5,2) DEFAULT 0 NOT NULL,
    vigente_desde timestamp with time zone NOT NULL,
    vigente_hasta timestamp with time zone,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_configuracion_descuento CHECK (((porcentaje_descuento >= (0)::numeric) AND (porcentaje_descuento <= (100)::numeric))),
    CONSTRAINT chk_configuracion_servicio CHECK (((porcentaje_servicio >= (0)::numeric) AND (porcentaje_servicio <= (100)::numeric))),
    CONSTRAINT chk_configuracion_vigencia CHECK (((vigente_hasta IS NULL) OR (vigente_hasta > vigente_desde)))
);


--
-- Name: cotizacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cotizacion (
    id_cotizacion uuid NOT NULL,
    id_publicacion uuid NOT NULL,
    id_configuracion uuid NOT NULL,
    fecha_entrada date NOT NULL,
    fecha_salida date NOT NULL,
    cantidad_huespedes integer NOT NULL,
    precio_noche_aplicado numeric(12,2) NOT NULL,
    tarifa_limpieza numeric(12,2) DEFAULT 0 NOT NULL,
    fecha_cotizacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_cotizacion_fechas CHECK ((fecha_salida > fecha_entrada)),
    CONSTRAINT chk_cotizacion_huespedes CHECK ((cantidad_huespedes > 0)),
    CONSTRAINT chk_cotizacion_limpieza CHECK ((tarifa_limpieza >= (0)::numeric)),
    CONSTRAINT chk_cotizacion_precio CHECK ((precio_noche_aplicado > (0)::numeric))
);


--
-- Name: cotizacion_adicional; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cotizacion_adicional (
    id_cotizacion uuid NOT NULL,
    id_adicional uuid NOT NULL,
    cantidad integer NOT NULL,
    precio_aplicado numeric(12,2) NOT NULL,
    CONSTRAINT chk_cotad_cantidad CHECK ((cantidad > 0)),
    CONSTRAINT chk_cotad_precio CHECK ((precio_aplicado >= (0)::numeric))
);


--
-- Name: exportacion_reporte; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.exportacion_reporte (
    id_exportacion uuid NOT NULL,
    id_administrador uuid NOT NULL,
    tipo_reporte character varying(50) NOT NULL,
    formato character varying(10) NOT NULL,
    filtros jsonb,
    estado character varying(30) DEFAULT 'SOLICITADO'::character varying NOT NULL,
    fecha_solicitud timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_corte_datos timestamp with time zone,
    fecha_generacion timestamp with time zone,
    ruta_archivo character varying(500),
    CONSTRAINT chk_exportacion_estado CHECK (((estado)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'EN_PROCESO'::character varying, 'COMPLETADO'::character varying, 'FALLIDO'::character varying])::text[]))),
    CONSTRAINT chk_exportacion_fechas CHECK ((((fecha_corte_datos IS NULL) OR (fecha_corte_datos >= fecha_solicitud)) AND ((fecha_generacion IS NULL) OR (fecha_generacion >= fecha_solicitud)))),
    CONSTRAINT chk_exportacion_formato CHECK (((formato)::text = ANY ((ARRAY['PDF'::character varying, 'EXCEL'::character varying])::text[]))),
    CONSTRAINT chk_exportacion_resultado CHECK ((((estado)::text <> 'COMPLETADO'::text) OR ((fecha_generacion IS NOT NULL) AND (fecha_corte_datos IS NOT NULL) AND (ruta_archivo IS NOT NULL))))
);


--
-- Name: imagen_publicacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.imagen_publicacion (
    id_imagen uuid NOT NULL,
    id_publicacion uuid NOT NULL,
    ruta_imagen character varying(500) NOT NULL,
    orden integer NOT NULL,
    es_portada boolean DEFAULT false NOT NULL,
    CONSTRAINT chk_imagen_orden CHECK ((orden > 0))
);


--
-- Name: noche_reservada; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.noche_reservada (
    id_noche uuid NOT NULL,
    id_alojamiento uuid NOT NULL,
    fecha date NOT NULL
);


--
-- Name: notificacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.notificacion (
    id_notificacion uuid NOT NULL,
    id_usuario uuid NOT NULL,
    id_reserva uuid,
    id_publicacion uuid,
    tipo character varying(50) NOT NULL,
    mensaje text NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_lectura timestamp with time zone,
    CONSTRAINT chk_notificacion_fecha CHECK (((fecha_lectura IS NULL) OR (fecha_lectura >= fecha_creacion)))
);


--
-- Name: operacion_pendiente; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.operacion_pendiente (
    id_operacion uuid NOT NULL,
    id_reserva uuid NOT NULL,
    id_pago uuid,
    tipo_operacion character varying(40) DEFAULT 'CONCILIAR_PAGO'::character varying NOT NULL,
    estado character varying(20) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    intentos integer DEFAULT 0 NOT NULL,
    proxima_ejecucion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ultima_ejecucion timestamp with time zone,
    ultimo_error text,
    clave_idempotencia character varying(100) NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_actualizacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_operacion_estado CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'EN_PROCESO'::character varying, 'COMPLETADA'::character varying, 'FALLIDA'::character varying])::text[]))),
    CONSTRAINT chk_operacion_intentos CHECK ((intentos >= 0)),
    CONSTRAINT chk_operacion_tipo CHECK (((tipo_operacion)::text = 'CONCILIAR_PAGO'::text))
);


--
-- Name: pago; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.pago (
    id_pago uuid NOT NULL,
    id_reserva uuid NOT NULL,
    referencia_stripe character varying(255),
    clave_idempotencia character varying(100) NOT NULL,
    monto numeric(12,2) NOT NULL,
    estado character varying(30) DEFAULT 'PENDIENTE'::character varying NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_pago timestamp with time zone,
    CONSTRAINT chk_pago_estado CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'PAGADO'::character varying, 'FALLIDO'::character varying, 'REEMBOLSADO'::character varying])::text[]))),
    CONSTRAINT chk_pago_fecha CHECK (((fecha_pago IS NULL) OR (fecha_pago >= fecha_creacion))),
    CONSTRAINT chk_pago_monto CHECK ((monto > (0)::numeric))
);


--
-- Name: periodo_disponibilidad; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.periodo_disponibilidad (
    id_periodo uuid NOT NULL,
    id_alojamiento uuid NOT NULL,
    fecha_inicio date NOT NULL,
    fecha_fin date NOT NULL,
    habilitado boolean DEFAULT true NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_periodo_fechas CHECK ((fecha_fin > fecha_inicio))
);


--
-- Name: politica_cancelacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.politica_cancelacion (
    id_politica uuid NOT NULL,
    nombre character varying(50) NOT NULL,
    version integer NOT NULL,
    descripcion text,
    anticipacion_horas integer,
    porcentaje_devolucion numeric(5,2) NOT NULL,
    activa boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_politica_anticipacion CHECK (((anticipacion_horas IS NULL) OR (anticipacion_horas >= 0))),
    CONSTRAINT chk_politica_devolucion CHECK (((porcentaje_devolucion >= (0)::numeric) AND (porcentaje_devolucion <= (100)::numeric))),
    CONSTRAINT chk_politica_version CHECK ((version > 0))
);


--
-- Name: publicacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.publicacion (
    id_publicacion uuid NOT NULL,
    id_alojamiento uuid NOT NULL,
    id_politica uuid NOT NULL,
    titulo character varying(200) NOT NULL,
    descripcion text NOT NULL,
    reglas text NOT NULL,
    precio_por_noche numeric(12,2) NOT NULL,
    tarifa_limpieza numeric(12,2) DEFAULT 0 NOT NULL,
    modo_aceptacion character varying(30) NOT NULL,
    estado character varying(30) DEFAULT 'BORRADOR'::character varying NOT NULL,
    fecha_creacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_actualizacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_publicacion_aceptacion CHECK (((modo_aceptacion)::text = ANY ((ARRAY['INMEDIATA'::character varying, 'APROBACION'::character varying])::text[]))),
    CONSTRAINT chk_publicacion_estado CHECK (((estado)::text = ANY ((ARRAY['BORRADOR'::character varying, 'PENDIENTE_REVISION'::character varying, 'ACTIVA'::character varying, 'PAUSADA'::character varying, 'BLOQUEADA'::character varying])::text[]))),
    CONSTRAINT chk_publicacion_limpieza CHECK ((tarifa_limpieza >= (0)::numeric)),
    CONSTRAINT chk_publicacion_precio CHECK ((precio_por_noche > (0)::numeric))
);


--
-- Name: publicacion_servicio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.publicacion_servicio (
    id_publicacion uuid NOT NULL,
    id_servicio uuid NOT NULL
);


--
-- Name: resena; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.resena (
    id_resena uuid NOT NULL,
    id_reserva uuid NOT NULL,
    calificacion integer NOT NULL,
    comentario text NOT NULL,
    visible boolean DEFAULT true NOT NULL,
    fecha_publicacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    id_administrador_moderador uuid,
    motivo_ocultamiento text,
    fecha_ocultamiento timestamp with time zone,
    CONSTRAINT chk_resena_calificacion CHECK (((calificacion >= 1) AND (calificacion <= 5))),
    CONSTRAINT chk_resena_moderacion CHECK ((((visible = true) AND (id_administrador_moderador IS NULL) AND (motivo_ocultamiento IS NULL) AND (fecha_ocultamiento IS NULL)) OR ((visible = false) AND (id_administrador_moderador IS NOT NULL) AND (motivo_ocultamiento IS NOT NULL) AND (fecha_ocultamiento IS NOT NULL))))
);


--
-- Name: reserva; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reserva (
    id_reserva uuid NOT NULL,
    id_huesped uuid NOT NULL,
    id_cotizacion uuid NOT NULL,
    id_politica_aplicada uuid NOT NULL,
    modo_aceptacion character varying(30) NOT NULL,
    estado character varying(30) NOT NULL,
    retencion_hasta timestamp with time zone,
    clave_idempotencia character varying(100) NOT NULL,
    requiere_revision_manual boolean DEFAULT false NOT NULL,
    fecha_solicitud timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_confirmacion timestamp with time zone,
    CONSTRAINT chk_reserva_aceptacion CHECK (((modo_aceptacion)::text = ANY ((ARRAY['INMEDIATA'::character varying, 'APROBACION'::character varying])::text[]))),
    CONSTRAINT chk_reserva_confirmacion CHECK (((fecha_confirmacion IS NULL) OR (fecha_confirmacion >= fecha_solicitud))),
    CONSTRAINT chk_reserva_estado CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE_PAGO'::character varying, 'PENDIENTE_APROBACION'::character varying, 'CONFIRMADA'::character varying, 'RECHAZADA'::character varying, 'CANCELADA'::character varying, 'COMPLETADA'::character varying, 'EXPIRADA'::character varying])::text[])))
);


--
-- Name: reserva_noche; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.reserva_noche (
    id_noche uuid NOT NULL,
    id_reserva uuid NOT NULL
);


--
-- Name: revision_publicacion; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.revision_publicacion (
    id_revision uuid NOT NULL,
    id_publicacion uuid NOT NULL,
    id_administrador uuid NOT NULL,
    accion character varying(50) NOT NULL,
    resultado character varying(30) NOT NULL,
    motivo text,
    fecha_revision timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: rol; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.rol (
    id_rol uuid NOT NULL,
    nombre character varying(50) NOT NULL,
    descripcion text
);


--
-- Name: servicio; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.servicio (
    id_servicio uuid NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text
);


--
-- Name: servicio_adicional; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.servicio_adicional (
    id_adicional uuid NOT NULL,
    id_publicacion uuid NOT NULL,
    nombre character varying(100) NOT NULL,
    descripcion text,
    costo numeric(12,2) NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT chk_adicional_costo CHECK ((costo >= (0)::numeric))
);


--
-- Name: usuario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuario (
    id_usuario uuid NOT NULL,
    id_rol uuid NOT NULL,
    nombre character varying(50) NOT NULL,
    apellido character varying(50) NOT NULL,
    correo character varying(254) NOT NULL,
    tipo_documento character varying(30) NOT NULL,
    documento_cifrado bytea NOT NULL,
    documento_nonce bytea NOT NULL,
    documento_hash bytea NOT NULL,
    telefono character varying(20) NOT NULL,
    contrasena_hash character varying(255) NOT NULL,
    correo_verificado boolean DEFAULT false NOT NULL,
    estado character varying(30) DEFAULT 'PENDIENTE_VERIFICACION'::character varying NOT NULL,
    restringido boolean DEFAULT false NOT NULL,
    intentos_fallidos integer DEFAULT 0 NOT NULL,
    motivo_restriccion text,
    fecha_registro timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    fecha_actualizacion timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    motivo_bloqueo character varying(255),
    CONSTRAINT chk_documento_hash CHECK ((octet_length(documento_hash) = 32)),
    CONSTRAINT chk_documento_nonce CHECK ((octet_length(documento_nonce) = 12)),
    CONSTRAINT chk_usuario_estado CHECK (((estado)::text = ANY ((ARRAY['PENDIENTE_VERIFICACION'::character varying, 'ACTIVA'::character varying, 'INACTIVA'::character varying])::text[]))),
    CONSTRAINT chk_usuario_intentos CHECK ((intentos_fallidos >= 0)),
    CONSTRAINT chk_usuario_motivo CHECK (((restringido AND (motivo_restriccion IS NOT NULL) AND (length(btrim(motivo_restriccion)) > 0)) OR ((NOT restringido) AND (motivo_restriccion IS NULL))))
);


--
-- Name: verificacion_correo; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.verificacion_correo (
    id_verificacion uuid NOT NULL,
    id_usuario uuid NOT NULL,
    codigo_hash character varying(255) NOT NULL,
    fecha_expiracion timestamp with time zone NOT NULL,
    verificado boolean DEFAULT false NOT NULL,
    intentos integer DEFAULT 0 NOT NULL,
    CONSTRAINT chk_intentos_verificacion CHECK ((intentos >= 0))
);


--
-- Name: alojamiento alojamiento_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alojamiento
    ADD CONSTRAINT alojamiento_pkey PRIMARY KEY (id_alojamiento);


--
-- Name: bandeja_salida_auditoria bandeja_salida_auditoria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bandeja_salida_auditoria
    ADD CONSTRAINT bandeja_salida_auditoria_pkey PRIMARY KEY (id_evento);


--
-- Name: cancelacion cancelacion_id_reserva_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cancelacion
    ADD CONSTRAINT cancelacion_id_reserva_key UNIQUE (id_reserva);


--
-- Name: cancelacion cancelacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cancelacion
    ADD CONSTRAINT cancelacion_pkey PRIMARY KEY (id_cancelacion);


--
-- Name: configuracion_tarifa configuracion_tarifa_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.configuracion_tarifa
    ADD CONSTRAINT configuracion_tarifa_pkey PRIMARY KEY (id_configuracion);


--
-- Name: cotizacion_adicional cotizacion_adicional_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion_adicional
    ADD CONSTRAINT cotizacion_adicional_pkey PRIMARY KEY (id_cotizacion, id_adicional);


--
-- Name: cotizacion cotizacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion
    ADD CONSTRAINT cotizacion_pkey PRIMARY KEY (id_cotizacion);


--
-- Name: exportacion_reporte exportacion_reporte_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exportacion_reporte
    ADD CONSTRAINT exportacion_reporte_pkey PRIMARY KEY (id_exportacion);


--
-- Name: imagen_publicacion imagen_publicacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.imagen_publicacion
    ADD CONSTRAINT imagen_publicacion_pkey PRIMARY KEY (id_imagen);


--
-- Name: noche_reservada noche_reservada_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noche_reservada
    ADD CONSTRAINT noche_reservada_pkey PRIMARY KEY (id_noche);


--
-- Name: notificacion notificacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_pkey PRIMARY KEY (id_notificacion);


--
-- Name: operacion_pendiente operacion_pendiente_clave_idempotencia_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operacion_pendiente
    ADD CONSTRAINT operacion_pendiente_clave_idempotencia_key UNIQUE (clave_idempotencia);


--
-- Name: operacion_pendiente operacion_pendiente_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operacion_pendiente
    ADD CONSTRAINT operacion_pendiente_pkey PRIMARY KEY (id_operacion);


--
-- Name: pago pago_clave_idempotencia_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_clave_idempotencia_key UNIQUE (clave_idempotencia);


--
-- Name: pago pago_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_pkey PRIMARY KEY (id_pago);


--
-- Name: pago pago_referencia_stripe_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_referencia_stripe_key UNIQUE (referencia_stripe);


--
-- Name: periodo_disponibilidad periodo_disponibilidad_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.periodo_disponibilidad
    ADD CONSTRAINT periodo_disponibilidad_pkey PRIMARY KEY (id_periodo);


--
-- Name: politica_cancelacion politica_cancelacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.politica_cancelacion
    ADD CONSTRAINT politica_cancelacion_pkey PRIMARY KEY (id_politica);


--
-- Name: publicacion publicacion_id_alojamiento_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publicacion
    ADD CONSTRAINT publicacion_id_alojamiento_key UNIQUE (id_alojamiento);


--
-- Name: publicacion publicacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publicacion
    ADD CONSTRAINT publicacion_pkey PRIMARY KEY (id_publicacion);


--
-- Name: publicacion_servicio publicacion_servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publicacion_servicio
    ADD CONSTRAINT publicacion_servicio_pkey PRIMARY KEY (id_publicacion, id_servicio);


--
-- Name: resena resena_id_reserva_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.resena
    ADD CONSTRAINT resena_id_reserva_key UNIQUE (id_reserva);


--
-- Name: resena resena_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.resena
    ADD CONSTRAINT resena_pkey PRIMARY KEY (id_resena);


--
-- Name: reserva reserva_clave_idempotencia_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_clave_idempotencia_key UNIQUE (clave_idempotencia);


--
-- Name: reserva reserva_id_cotizacion_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_id_cotizacion_key UNIQUE (id_cotizacion);


--
-- Name: reserva_noche reserva_noche_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva_noche
    ADD CONSTRAINT reserva_noche_pkey PRIMARY KEY (id_noche);


--
-- Name: reserva reserva_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_pkey PRIMARY KEY (id_reserva);


--
-- Name: revision_publicacion revision_publicacion_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.revision_publicacion
    ADD CONSTRAINT revision_publicacion_pkey PRIMARY KEY (id_revision);


--
-- Name: rol rol_nombre_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_nombre_key UNIQUE (nombre);


--
-- Name: rol rol_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.rol
    ADD CONSTRAINT rol_pkey PRIMARY KEY (id_rol);


--
-- Name: servicio_adicional servicio_adicional_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicio_adicional
    ADD CONSTRAINT servicio_adicional_pkey PRIMARY KEY (id_adicional);


--
-- Name: servicio servicio_nombre_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT servicio_nombre_key UNIQUE (nombre);


--
-- Name: servicio servicio_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicio
    ADD CONSTRAINT servicio_pkey PRIMARY KEY (id_servicio);


--
-- Name: servicio_adicional uq_adicional_nombre; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicio_adicional
    ADD CONSTRAINT uq_adicional_nombre UNIQUE (id_publicacion, nombre);


--
-- Name: imagen_publicacion uq_imagen_orden; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.imagen_publicacion
    ADD CONSTRAINT uq_imagen_orden UNIQUE (id_publicacion, orden);


--
-- Name: noche_reservada uq_noche_alojamiento_fecha; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noche_reservada
    ADD CONSTRAINT uq_noche_alojamiento_fecha UNIQUE (id_alojamiento, fecha);


--
-- Name: politica_cancelacion uq_politica_nombre_version; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.politica_cancelacion
    ADD CONSTRAINT uq_politica_nombre_version UNIQUE (nombre, version);


--
-- Name: usuario usuario_documento_hash_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_documento_hash_key UNIQUE (documento_hash);


--
-- Name: usuario usuario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_pkey PRIMARY KEY (id_usuario);


--
-- Name: verificacion_correo verificacion_correo_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verificacion_correo
    ADD CONSTRAINT verificacion_correo_pkey PRIMARY KEY (id_verificacion);


--
-- Name: idx_adicional_publicacion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_adicional_publicacion ON public.servicio_adicional USING btree (id_publicacion);


--
-- Name: idx_alojamiento_anfitrion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_alojamiento_anfitrion ON public.alojamiento USING btree (id_anfitrion);


--
-- Name: idx_alojamiento_busqueda; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_alojamiento_busqueda ON public.alojamiento USING btree (ciudad, tipo_alojamiento, capacidad);


--
-- Name: idx_cancelacion_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cancelacion_usuario ON public.cancelacion USING btree (id_usuario);


--
-- Name: idx_cotizacion_configuracion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cotizacion_configuracion ON public.cotizacion USING btree (id_configuracion);


--
-- Name: idx_cotizacion_publicacion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_cotizacion_publicacion ON public.cotizacion USING btree (id_publicacion);


--
-- Name: idx_exportacion_administrador_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exportacion_administrador_fecha ON public.exportacion_reporte USING btree (id_administrador, fecha_solicitud DESC);


--
-- Name: idx_exportacion_worker; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_exportacion_worker ON public.exportacion_reporte USING btree (estado, fecha_solicitud) WHERE ((estado)::text = ANY ((ARRAY['SOLICITADO'::character varying, 'EN_PROCESO'::character varying])::text[]));


--
-- Name: idx_imagen_publicacion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_imagen_publicacion ON public.imagen_publicacion USING btree (id_publicacion);


--
-- Name: idx_notificacion_usuario_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_notificacion_usuario_fecha ON public.notificacion USING btree (id_usuario, fecha_creacion DESC);


--
-- Name: idx_operacion_pendiente_worker; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_operacion_pendiente_worker ON public.operacion_pendiente USING btree (estado, proxima_ejecucion);


--
-- Name: idx_outbox_worker; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_outbox_worker ON public.bandeja_salida_auditoria USING btree (estado_envio, proxima_ejecucion) WHERE ((estado_envio)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'ERROR'::character varying])::text[]));


--
-- Name: idx_pago_reserva; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pago_reserva ON public.pago USING btree (id_reserva);


--
-- Name: idx_periodo_alojamiento_fechas; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_periodo_alojamiento_fechas ON public.periodo_disponibilidad USING btree (id_alojamiento, fecha_inicio, fecha_fin);


--
-- Name: idx_publicacion_politica; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_publicacion_politica ON public.publicacion USING btree (id_politica);


--
-- Name: idx_publicacion_precio_activa; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_publicacion_precio_activa ON public.publicacion USING btree (precio_por_noche) WHERE ((estado)::text = 'ACTIVA'::text);


--
-- Name: idx_pubserv_servicio; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_pubserv_servicio ON public.publicacion_servicio USING btree (id_servicio);


--
-- Name: idx_resena_moderador; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_resena_moderador ON public.resena USING btree (id_administrador_moderador);


--
-- Name: idx_reserva_estado_retencion; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reserva_estado_retencion ON public.reserva USING btree (estado, retencion_hasta) WHERE ((estado)::text = ANY ((ARRAY['PENDIENTE_PAGO'::character varying, 'PENDIENTE_APROBACION'::character varying])::text[]));


--
-- Name: idx_reserva_huesped; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reserva_huesped ON public.reserva USING btree (id_huesped);


--
-- Name: idx_reserva_noche_reserva; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_reserva_noche_reserva ON public.reserva_noche USING btree (id_reserva);


--
-- Name: idx_revision_publicacion_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_revision_publicacion_fecha ON public.revision_publicacion USING btree (id_publicacion, fecha_revision DESC);


--
-- Name: idx_verificacion_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_verificacion_usuario ON public.verificacion_correo USING btree (id_usuario);


--
-- Name: uq_configuracion_vigente; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_configuracion_vigente ON public.configuracion_tarifa USING btree ((1)) WHERE (vigente_hasta IS NULL);


--
-- Name: uq_imagen_portada; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_imagen_portada ON public.imagen_publicacion USING btree (id_publicacion) WHERE (es_portada = true);


--
-- Name: uq_operacion_activa_reserva; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_operacion_activa_reserva ON public.operacion_pendiente USING btree (id_reserva) WHERE ((estado)::text = ANY ((ARRAY['PENDIENTE'::character varying, 'EN_PROCESO'::character varying])::text[]));


--
-- Name: uq_pago_exitoso_reserva; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_pago_exitoso_reserva ON public.pago USING btree (id_reserva) WHERE ((estado)::text = ANY ((ARRAY['PAGADO'::character varying, 'REEMBOLSADO'::character varying])::text[]));


--
-- Name: uq_politica_version_activa; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_politica_version_activa ON public.politica_cancelacion USING btree (nombre) WHERE (activa = true);


--
-- Name: uq_usuario_correo_ci; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_usuario_correo_ci ON public.usuario USING btree (lower((correo)::text));


--
-- Name: uq_usuario_telefono; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_usuario_telefono ON public.usuario USING btree (telefono);


--
-- Name: alojamiento alojamiento_id_anfitrion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.alojamiento
    ADD CONSTRAINT alojamiento_id_anfitrion_fkey FOREIGN KEY (id_anfitrion) REFERENCES public.usuario(id_usuario);


--
-- Name: bandeja_salida_auditoria bandeja_salida_auditoria_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.bandeja_salida_auditoria
    ADD CONSTRAINT bandeja_salida_auditoria_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- Name: cancelacion cancelacion_id_reserva_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cancelacion
    ADD CONSTRAINT cancelacion_id_reserva_fkey FOREIGN KEY (id_reserva) REFERENCES public.reserva(id_reserva);


--
-- Name: cancelacion cancelacion_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cancelacion
    ADD CONSTRAINT cancelacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- Name: configuracion_tarifa configuracion_tarifa_id_administrador_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.configuracion_tarifa
    ADD CONSTRAINT configuracion_tarifa_id_administrador_fkey FOREIGN KEY (id_administrador) REFERENCES public.usuario(id_usuario);


--
-- Name: cotizacion_adicional cotizacion_adicional_id_adicional_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion_adicional
    ADD CONSTRAINT cotizacion_adicional_id_adicional_fkey FOREIGN KEY (id_adicional) REFERENCES public.servicio_adicional(id_adicional);


--
-- Name: cotizacion_adicional cotizacion_adicional_id_cotizacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion_adicional
    ADD CONSTRAINT cotizacion_adicional_id_cotizacion_fkey FOREIGN KEY (id_cotizacion) REFERENCES public.cotizacion(id_cotizacion);


--
-- Name: cotizacion cotizacion_id_configuracion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion
    ADD CONSTRAINT cotizacion_id_configuracion_fkey FOREIGN KEY (id_configuracion) REFERENCES public.configuracion_tarifa(id_configuracion);


--
-- Name: cotizacion cotizacion_id_publicacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cotizacion
    ADD CONSTRAINT cotizacion_id_publicacion_fkey FOREIGN KEY (id_publicacion) REFERENCES public.publicacion(id_publicacion);


--
-- Name: exportacion_reporte exportacion_reporte_id_administrador_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.exportacion_reporte
    ADD CONSTRAINT exportacion_reporte_id_administrador_fkey FOREIGN KEY (id_administrador) REFERENCES public.usuario(id_usuario);


--
-- Name: verificacion_correo fk_verificacion_usuario; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.verificacion_correo
    ADD CONSTRAINT fk_verificacion_usuario FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario) ON DELETE CASCADE;


--
-- Name: imagen_publicacion imagen_publicacion_id_publicacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.imagen_publicacion
    ADD CONSTRAINT imagen_publicacion_id_publicacion_fkey FOREIGN KEY (id_publicacion) REFERENCES public.publicacion(id_publicacion);


--
-- Name: noche_reservada noche_reservada_id_alojamiento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.noche_reservada
    ADD CONSTRAINT noche_reservada_id_alojamiento_fkey FOREIGN KEY (id_alojamiento) REFERENCES public.alojamiento(id_alojamiento);


--
-- Name: notificacion notificacion_id_publicacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_id_publicacion_fkey FOREIGN KEY (id_publicacion) REFERENCES public.publicacion(id_publicacion);


--
-- Name: notificacion notificacion_id_reserva_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_id_reserva_fkey FOREIGN KEY (id_reserva) REFERENCES public.reserva(id_reserva);


--
-- Name: notificacion notificacion_id_usuario_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.notificacion
    ADD CONSTRAINT notificacion_id_usuario_fkey FOREIGN KEY (id_usuario) REFERENCES public.usuario(id_usuario);


--
-- Name: operacion_pendiente operacion_pendiente_id_pago_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operacion_pendiente
    ADD CONSTRAINT operacion_pendiente_id_pago_fkey FOREIGN KEY (id_pago) REFERENCES public.pago(id_pago);


--
-- Name: operacion_pendiente operacion_pendiente_id_reserva_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.operacion_pendiente
    ADD CONSTRAINT operacion_pendiente_id_reserva_fkey FOREIGN KEY (id_reserva) REFERENCES public.reserva(id_reserva);


--
-- Name: pago pago_id_reserva_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.pago
    ADD CONSTRAINT pago_id_reserva_fkey FOREIGN KEY (id_reserva) REFERENCES public.reserva(id_reserva);


--
-- Name: periodo_disponibilidad periodo_disponibilidad_id_alojamiento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.periodo_disponibilidad
    ADD CONSTRAINT periodo_disponibilidad_id_alojamiento_fkey FOREIGN KEY (id_alojamiento) REFERENCES public.alojamiento(id_alojamiento);


--
-- Name: publicacion publicacion_id_alojamiento_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publicacion
    ADD CONSTRAINT publicacion_id_alojamiento_fkey FOREIGN KEY (id_alojamiento) REFERENCES public.alojamiento(id_alojamiento);


--
-- Name: publicacion publicacion_id_politica_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publicacion
    ADD CONSTRAINT publicacion_id_politica_fkey FOREIGN KEY (id_politica) REFERENCES public.politica_cancelacion(id_politica);


--
-- Name: publicacion_servicio publicacion_servicio_id_publicacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publicacion_servicio
    ADD CONSTRAINT publicacion_servicio_id_publicacion_fkey FOREIGN KEY (id_publicacion) REFERENCES public.publicacion(id_publicacion);


--
-- Name: publicacion_servicio publicacion_servicio_id_servicio_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.publicacion_servicio
    ADD CONSTRAINT publicacion_servicio_id_servicio_fkey FOREIGN KEY (id_servicio) REFERENCES public.servicio(id_servicio);


--
-- Name: resena resena_id_administrador_moderador_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.resena
    ADD CONSTRAINT resena_id_administrador_moderador_fkey FOREIGN KEY (id_administrador_moderador) REFERENCES public.usuario(id_usuario);


--
-- Name: resena resena_id_reserva_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.resena
    ADD CONSTRAINT resena_id_reserva_fkey FOREIGN KEY (id_reserva) REFERENCES public.reserva(id_reserva);


--
-- Name: reserva reserva_id_cotizacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_id_cotizacion_fkey FOREIGN KEY (id_cotizacion) REFERENCES public.cotizacion(id_cotizacion);


--
-- Name: reserva reserva_id_huesped_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_id_huesped_fkey FOREIGN KEY (id_huesped) REFERENCES public.usuario(id_usuario);


--
-- Name: reserva reserva_id_politica_aplicada_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva
    ADD CONSTRAINT reserva_id_politica_aplicada_fkey FOREIGN KEY (id_politica_aplicada) REFERENCES public.politica_cancelacion(id_politica);


--
-- Name: reserva_noche reserva_noche_id_noche_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva_noche
    ADD CONSTRAINT reserva_noche_id_noche_fkey FOREIGN KEY (id_noche) REFERENCES public.noche_reservada(id_noche) ON DELETE CASCADE;


--
-- Name: reserva_noche reserva_noche_id_reserva_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.reserva_noche
    ADD CONSTRAINT reserva_noche_id_reserva_fkey FOREIGN KEY (id_reserva) REFERENCES public.reserva(id_reserva);


--
-- Name: revision_publicacion revision_publicacion_id_administrador_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.revision_publicacion
    ADD CONSTRAINT revision_publicacion_id_administrador_fkey FOREIGN KEY (id_administrador) REFERENCES public.usuario(id_usuario);


--
-- Name: revision_publicacion revision_publicacion_id_publicacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.revision_publicacion
    ADD CONSTRAINT revision_publicacion_id_publicacion_fkey FOREIGN KEY (id_publicacion) REFERENCES public.publicacion(id_publicacion);


--
-- Name: servicio_adicional servicio_adicional_id_publicacion_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.servicio_adicional
    ADD CONSTRAINT servicio_adicional_id_publicacion_fkey FOREIGN KEY (id_publicacion) REFERENCES public.publicacion(id_publicacion);


--
-- Name: usuario usuario_id_rol_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuario
    ADD CONSTRAINT usuario_id_rol_fkey FOREIGN KEY (id_rol) REFERENCES public.rol(id_rol);


--
-- PostgreSQL database dump complete
--