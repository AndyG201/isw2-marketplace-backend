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
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

@Repository
public class PublicacionBusquedaJdbcRepository implements PublicacionBusquedaRepository {

    private static final String FROM_AND_WHERE = """
            FROM public.publicacion p
            INNER JOIN public.alojamiento a
                ON a.id_alojamiento = p.id_alojamiento
            WHERE p.estado = 'ACTIVA'
            """;

    private static final String CON_TILDE = "áàäâéèëêíìïîóòöôúùüûñç";
    private static final String SIN_TILDE = "aaaaeeeeiiiioooouuuunc";

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

        StringBuilder condiciones = new StringBuilder(FROM_AND_WHERE);
        MapSqlParameterSource parametros = new MapSqlParameterSource()
                .addValue("tamano", criteria.tamano())
                .addValue(
                        "offset",
                        (long) criteria.pagina() * criteria.tamano()
                );

        if (criteria.ciudad() != null) {
            condiciones.append(" AND translate(lower(a.ciudad), :conTilde, :sinTilde)"
                    + " LIKE :ciudad ESCAPE '\\'");
            parametros.addValue("conTilde", CON_TILDE);
            parametros.addValue("sinTilde", SIN_TILDE);
            parametros.addValue("ciudad", "%" + patronCiudad(criteria.ciudad()) + "%");
        }

        if (criteria.tipoAlojamiento() != null) {
            condiciones.append(
                    " AND lower(a.tipo_alojamiento) = :tipoAlojamiento"
            );
            parametros.addValue(
                    "tipoAlojamiento",
                    criteria.tipoAlojamiento()
                            .toLowerCase(java.util.Locale.ROOT)
            );
        }

        if (criteria.capacidad() != null) {
            condiciones.append(" AND a.capacidad >= :capacidad");
            parametros.addValue("capacidad", criteria.capacidad());
        }

        if (criteria.precioMinimo() != null) {
            condiciones.append(
                    " AND p.precio_por_noche >= :precioMinimo"
            );
            parametros.addValue(
                    "precioMinimo",
                    criteria.precioMinimo()
            );
        }

        if (criteria.precioMaximo() != null) {
            condiciones.append(
                    " AND p.precio_por_noche <= :precioMaximo"
            );
            parametros.addValue(
                    "precioMaximo",
                    criteria.precioMaximo()
            );
        }

        if (!criteria.servicios().isEmpty()) {
            condiciones.append("""
                     AND p.id_publicacion IN (
                        SELECT ps.id_publicacion
                        FROM public.publicacion_servicio ps
                        WHERE ps.id_servicio IN (:servicios)
                        GROUP BY ps.id_publicacion
                        HAVING COUNT(DISTINCT ps.id_servicio)
                            = :cantidadServicios
                    )
                    """);
            parametros.addValue("servicios", criteria.servicios());
            parametros.addValue(
                    "cantidadServicios",
                    criteria.servicios().size()
            );
        }

        if (criteria.fechaInicio() != null) {
            condiciones.append("""
                     AND NOT EXISTS (
                        SELECT 1
                        FROM generate_series(
                            CAST(:fechaInicio AS date),
                            CAST(:fechaFin AS date) - 1,
                            INTERVAL '1 day'
                        ) AS noche(fecha)
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM public.periodo_disponibilidad pd
                            WHERE pd.id_alojamiento = a.id_alojamiento
                              AND pd.habilitado = true
                              AND pd.fecha_inicio
                                  <= CAST(noche.fecha AS date)
                              AND pd.fecha_fin
                                  > CAST(noche.fecha AS date)
                        )
                        OR EXISTS (
                            SELECT 1
                            FROM public.noche_reservada nr
                            INNER JOIN public.reserva_noche rn
                                ON rn.id_noche = nr.id_noche
                            INNER JOIN public.reserva r
                                ON r.id_reserva = rn.id_reserva
                            WHERE nr.id_alojamiento = a.id_alojamiento
                              AND nr.fecha = CAST(noche.fecha AS date)
                              AND (
                                  r.estado IN (
                                      'PENDIENTE_APROBACION',
                                      'CONFIRMADA',
                                      'COMPLETADA'
                                  )
                                  OR (
                                      r.estado = 'PENDIENTE_PAGO'
                                      AND r.retencion_hasta > CURRENT_TIMESTAMP
                                  )
                              )
                        )
                    )
                    """);
            parametros.addValue("fechaInicio", criteria.fechaInicio());
            parametros.addValue("fechaFin", criteria.fechaFin());
        }

        String where = condiciones.toString();

        Long total = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " + where,
                parametros,
                Long.class
        );

        List<PublicacionResumenResponse> publicaciones =
                jdbcTemplate.query(
                        """
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
                        """ + where
                                + " ORDER BY p.fecha_creacion DESC,"
                                + " p.id_publicacion ASC"
                                + " LIMIT :tamano OFFSET :offset",
                        parametros,
                        ROW_MAPPER
                );

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
