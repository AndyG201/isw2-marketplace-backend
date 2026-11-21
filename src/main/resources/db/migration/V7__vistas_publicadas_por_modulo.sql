CREATE OR REPLACE VIEW public.vista_configuracion_tarifa AS
SELECT ct.id_configuracion,
       ct.porcentaje_servicio,
       ct.porcentaje_descuento
FROM public.configuracion_tarifa ct;

CREATE OR REPLACE VIEW public.vista_alojamiento AS
SELECT a.id_alojamiento,
       a.id_anfitrion,
       a.ciudad,
       a.pais,
       a.codigo_pais,
       a.tipo_alojamiento,
       a.capacidad
FROM public.alojamiento a;

CREATE OR REPLACE VIEW public.vista_publicacion AS
SELECT p.id_publicacion,
       p.id_alojamiento,
       p.id_politica,
       p.titulo,
       p.estado,
       p.modo_aceptacion
FROM public.publicacion p;

CREATE OR REPLACE VIEW public.vista_cotizacion AS
SELECT c.id_cotizacion,
       c.id_publicacion,
       c.fecha_entrada,
       c.fecha_salida,
       c.cantidad_huespedes,
       ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
        + c.tarifa_limpieza
        + ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
           * t.porcentaje_servicio / 100)
        - ((c.precio_noche_aplicado * (c.fecha_salida - c.fecha_entrada))
           * t.porcentaje_descuento / 100)
        + COALESCE((SELECT SUM(ca.cantidad * ca.precio_aplicado)
                    FROM public.cotizacion_adicional ca
                    WHERE ca.id_cotizacion = c.id_cotizacion), 0)) AS monto_total
FROM public.cotizacion c
JOIN public.vista_configuracion_tarifa t ON t.id_configuracion = c.id_configuracion;

CREATE OR REPLACE VIEW public.vista_periodo_habilitado AS
SELECT pd.id_alojamiento,
       pd.fecha_inicio,
       pd.fecha_fin
FROM public.periodo_disponibilidad pd
WHERE pd.habilitado = TRUE;

CREATE OR REPLACE VIEW public.vista_reserva AS
SELECT r.id_reserva,
       r.id_huesped,
       r.id_cotizacion,
       r.estado
FROM public.reserva r;

CREATE OR REPLACE VIEW public.vista_noche_ocupada AS
SELECT DISTINCT nr.id_alojamiento,
       nr.fecha
FROM public.noche_reservada nr
JOIN public.reserva_noche rn ON rn.id_noche = nr.id_noche
JOIN public.reserva r ON r.id_reserva = rn.id_reserva
WHERE r.estado IN ('PENDIENTE_APROBACION', 'CONFIRMADA', 'COMPLETADA')
   OR (r.estado = 'PENDIENTE_PAGO' AND r.retencion_hasta > CURRENT_TIMESTAMP);

CREATE OR REPLACE VIEW public.vista_pago AS
SELECT pg.id_pago,
       pg.id_reserva,
       pg.monto,
       pg.estado,
       pg.fecha_pago
FROM public.pago pg;

CREATE OR REPLACE VIEW public.vista_resena AS
SELECT rs.id_resena,
       rs.id_reserva
FROM public.resena rs;

CREATE OR REPLACE VIEW public.vista_usuario AS
SELECT u.id_usuario,
       u.nombre,
       u.apellido,
       u.estado,
       u.correo_verificado,
       u.restringido,
       ro.nombre AS rol
FROM public.usuario u
LEFT JOIN public.rol ro ON ro.id_rol = u.id_rol;
