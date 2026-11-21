CREATE OR REPLACE VIEW public.vista_cancelacion AS
SELECT ca.id_cancelacion,
       ca.id_reserva,
       ca.id_usuario,
       ca.fecha_cancelacion,
       ca.motivo
FROM public.cancelacion ca;
