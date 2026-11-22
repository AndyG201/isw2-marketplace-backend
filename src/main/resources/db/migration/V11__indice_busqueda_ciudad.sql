-- Búsqueda de alojamientos por ciudad sin distinguir mayúsculas ni tildes.
--
-- Antes la consulta calculaba translate(lower(ciudad), ...) fila por fila y comparaba con LIKE '%texto%',
-- lo que impedía usar cualquier índice (idx_alojamiento_busqueda no sirve para ese filtro).
-- Ahora la ciudad normalizada se guarda en una columna generada y se indexa con trigramas, que sí aceleran LIKE '%texto%'.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE public.alojamiento
    ADD COLUMN ciudad_normalizada text
    GENERATED ALWAYS AS (
        translate(lower(ciudad), 'áàäâéèëêíìïîóòöôúùüûñç', 'aaaaeeeeiiiioooouuuunc')
    ) STORED;

CREATE INDEX idx_alojamiento_ciudad_trgm
    ON public.alojamiento USING gin (ciudad_normalizada gin_trgm_ops);

-- La vista que usa la búsqueda expone la columna nueva (al final, para no alterar las existentes).
CREATE OR REPLACE VIEW public.vista_alojamiento AS
SELECT a.id_alojamiento,
       a.id_anfitrion,
       a.ciudad,
       a.pais,
       a.codigo_pais,
       a.tipo_alojamiento,
       a.capacidad,
       a.ciudad_normalizada
FROM public.alojamiento a;
