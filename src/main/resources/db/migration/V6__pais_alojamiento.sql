ALTER TABLE alojamiento
    ADD COLUMN pais VARCHAR(120),
    ADD COLUMN codigo_pais VARCHAR(2);

UPDATE alojamiento a
SET pais = x.pais,
    codigo_pais = x.codigo
FROM (
    SELECT translate(lower(c.nombre), 'áàäâéèëêíìïîóòöôúùüûñç', 'aaaaeeeeiiiioooouuuunc') AS clave,
           min(p.nombre) AS pais,
           min(p.codigo_iso2) AS codigo
    FROM ciudad c
    JOIN pais p ON p.id_pais = c.id_pais
    GROUP BY 1
    HAVING count(DISTINCT p.id_pais) = 1
) x
WHERE x.clave = translate(lower(a.ciudad), 'áàäâéèëêíìïîóòöôúùüûñç', 'aaaaeeeeiiiioooouuuunc');
