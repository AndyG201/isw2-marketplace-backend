-- A6: comprobacion transaccional de V12; no deja registros de prueba.
\set ON_ERROR_STOP on
\echo [A6] Comprobando Flyway V12 y la estructura de public.evento_auditoria...

DO $verificar$
DECLARE
    columnas_presentes INTEGER;
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM public.flyway_schema_history
         WHERE version = '12' AND success = TRUE
    ) THEN
        RAISE EXCEPTION 'Flyway V12 aun no se ha aplicado correctamente';
    END IF;

    IF to_regclass('public.evento_auditoria') IS NULL THEN
        RAISE EXCEPTION 'No existe public.evento_auditoria';
    END IF;

    SELECT COUNT(*) INTO columnas_presentes
      FROM information_schema.columns
     WHERE table_schema = 'public'
       AND table_name = 'evento_auditoria'
       AND column_name IN (
           'id_evento', 'id_usuario', 'fecha_evento', 'accion',
           'resultado', 'entidad_afectada', 'id_entidad_afectada',
           'id_correlacion', 'clave_objeto', 'sha256', 'modo_retencion',
           'retencion_hasta', 'version_objeto'
       );

    IF columnas_presentes <> 13 THEN
        RAISE EXCEPTION 'Faltan columnas en public.evento_auditoria (% de 13)', columnas_presentes;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
         WHERE schemaname = 'public'
           AND tablename = 'evento_auditoria'
           AND indexname = 'idx_evento_auditoria_fecha'
    ) THEN
        RAISE EXCEPTION 'Falta el indice de paginacion por fecha';
    END IF;
END
$verificar$;

\echo [A6] Insertando y consultando un evento temporal (ROLLBACK al final)...
BEGIN;

INSERT INTO public.evento_auditoria (
    id_evento, id_usuario, fecha_evento, accion, resultado,
    entidad_afectada, id_entidad_afectada, id_correlacion,
    clave_objeto, sha256, modo_retencion, retencion_hasta,
    version_objeto
) VALUES (
    '00000000-0000-4000-8000-00000000a612',
    NULL,
    CURRENT_TIMESTAMP,
    'PRUEBA_A6',
    'EXITOSO',
    'PRUEBA',
    NULL,
    'correlacion-a6',
    'eventos/prueba-a6.json',
    repeat('a', 64),
    'COMPLIANCE',
    CURRENT_TIMESTAMP + INTERVAL '7 days',
    NULL
) ON CONFLICT (id_evento) DO NOTHING;

DO $verificar$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM public.evento_auditoria
         WHERE id_evento = '00000000-0000-4000-8000-00000000a612'
           AND accion = 'PRUEBA_A6'
           AND sha256 = repeat('a', 64)
           AND modo_retencion = 'COMPLIANCE'
    ) THEN
        RAISE EXCEPTION 'No se pudo insertar y consultar el evento temporal';
    END IF;
END
$verificar$;

ROLLBACK;
\echo [A6] OK: V12 presente, 13 columnas e insercion/consulta correctas. No quedaron datos de prueba.
