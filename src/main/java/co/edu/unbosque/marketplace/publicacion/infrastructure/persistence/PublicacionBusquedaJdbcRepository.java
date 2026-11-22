package co.edu.unbosque.marketplace.publicacion.infrastructure.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.publicacion.application.dto.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.application.dto.PublicacionResumenResponse;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionBusquedaRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

@Repository
public class PublicacionBusquedaJdbcRepository implements PublicacionBusquedaRepository {

    private static final UUID SIN_SERVICIOS = new UUID(0L, 0L);

    private static final String FILTROS = """
            FROM public.publicacion p
            INNER JOIN public.vista_alojamiento a
                ON a.id_alojamiento = p.id_alojamiento
            WHERE p.estado = 'ACTIVA'
              AND (CAST(:ciudad AS text) IS NULL
                   OR a.ciudad_normalizada
                      LIKE CAST(:ciudad AS text) ESCAPE '\\')
              AND (CAST(:tipoAlojamiento AS text) IS NULL
                   OR lower(a.tipo_alojamiento) = CAST(:tipoAlojamiento AS text))
              AND (CAST(:capacidad AS integer) IS NULL
                   OR a.capacidad >= CAST(:capacidad AS integer))
              AND (CAST(:precioMinimo AS numeric) IS NULL
                   OR p.precio_por_noche >= CAST(:precioMinimo AS numeric))
              AND (CAST(:precioMaximo AS numeric) IS NULL
                   OR p.precio_por_noche <= CAST(:precioMaximo AS numeric))
              AND (:cantidadServicios = 0
                   OR p.id_publicacion IN (
                       SELECT ps.id_publicacion
                       FROM public.publicacion_servicio ps
                       WHERE ps.id_servicio IN (:servicios)
                       GROUP BY ps.id_publicacion
                       HAVING COUNT(DISTINCT ps.id_servicio) = :cantidadServicios
                   ))
              AND (CAST(:fechaInicio AS date) IS NULL
                   OR NOT EXISTS (
                       SELECT 1
                       FROM generate_series(
                           CAST(:fechaInicio AS date),
                           CAST(:fechaFin AS date) - 1,
                           INTERVAL '1 day'
                       ) AS noche(fecha)
                       WHERE NOT EXISTS (
                           SELECT 1
                           FROM public.vista_periodo_habilitado pd
                           WHERE pd.id_alojamiento = a.id_alojamiento
                             AND pd.fecha_inicio <= CAST(noche.fecha AS date)
                             AND pd.fecha_fin > CAST(noche.fecha AS date)
                       )
                       OR EXISTS (
                           SELECT 1
                           FROM public.vista_noche_ocupada nr
                           WHERE nr.id_alojamiento = a.id_alojamiento
                             AND nr.fecha = CAST(noche.fecha AS date)
                       )
                   ))
            """;

    private static final String CONTAR = "SELECT COUNT(*) " + FILTROS;

    private static final String CONSULTAR = """
            SELECT p.id_publicacion,
                   a.id_alojamiento,
                   p.titulo,
                   a.ciudad,
                   a.pais,
                   a.codigo_pais,
                   a.tipo_alojamiento,
                   a.capacidad,
                   p.precio_por_noche,
                   p.estado,
                   p.fecha_creacion,
                   (
                       SELECT i.ruta_imagen
                       FROM public.imagen_publicacion i
                       WHERE i.id_publicacion = p.id_publicacion
                       ORDER BY i.es_portada DESC, i.orden ASC
                       LIMIT 1
                   ) AS imagen_portada
            """ + FILTROS + """
            ORDER BY p.fecha_creacion DESC, p.id_publicacion ASC
            LIMIT :tamano OFFSET :offset
            """;

    private static final RowMapper<PublicacionResumenResponse> ROW_MAPPER =
            PublicacionBusquedaJdbcRepository::mapearPublicacion;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PublicacionBusquedaJdbcRepository(
            NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public PaginaPublicacionesResponse buscar(
            PublicacionBusquedaCriteria criteria) {

        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("tamano", criteria.tamano())
                .addValue("offset", (long) criteria.pagina() * criteria.tamano())
                .addValue("ciudad", criteria.ciudad() == null
                        ? null
                        : "%" + patronCiudad(criteria.ciudad()) + "%")
                .addValue("tipoAlojamiento", criteria.tipoAlojamiento() == null
                        ? null
                        : criteria.tipoAlojamiento().toLowerCase(Locale.ROOT))
                .addValue("capacidad", criteria.capacidad())
                .addValue("precioMinimo", criteria.precioMinimo())
                .addValue("precioMaximo", criteria.precioMaximo())
                .addValue("servicios", criteria.servicios().isEmpty()
                        ? List.of(SIN_SERVICIOS)
                        : criteria.servicios())
                .addValue("cantidadServicios", criteria.servicios().size())
                .addValue("fechaInicio", criteria.fechaInicio())
                .addValue("fechaFin", criteria.fechaFin());

        Long total = jdbcTemplate.queryForObject(CONTAR, parametros, Long.class);

        List<PublicacionResumenResponse> publicaciones =
                jdbcTemplate.query(CONSULTAR, parametros, ROW_MAPPER);

        long cantidad = total == null ? 0 : total;
        int totalPaginas = (int) Math.min(
                Integer.MAX_VALUE,
                cantidad / criteria.tamano()
                        + (cantidad % criteria.tamano() == 0 ? 0 : 1)
        );

        String mensaje = cantidad == 0
                ? "No se encontraron publicaciones con los filtros indicados."
                : null;

        return new PaginaPublicacionesResponse(
                publicaciones,
                criteria.pagina(),
                criteria.tamano(),
                cantidad,
                totalPaginas,
                mensaje
        );
    }

    private static PublicacionResumenResponse mapearPublicacion(
            ResultSet rs,
            int rowNum) throws SQLException {
        return new PublicacionResumenResponse(
                rs.getObject("id_publicacion", UUID.class),
                rs.getObject("id_alojamiento", UUID.class),
                rs.getString("titulo"),
                rs.getString("ciudad"),
                rs.getString("tipo_alojamiento"),
                rs.getInt("capacidad"),
                rs.getBigDecimal("precio_por_noche"),
                rs.getString("estado"),
                rs.getObject(
                        "fecha_creacion",
                        java.time.OffsetDateTime.class
                ),
                rs.getString("imagen_portada")
        ).conPais(rs.getString("pais"), rs.getString("codigo_pais"));
    }

    static String patronCiudad(String ciudad) {
        String sinTildes = Normalizer.normalize(ciudad.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
        return sinTildes.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
