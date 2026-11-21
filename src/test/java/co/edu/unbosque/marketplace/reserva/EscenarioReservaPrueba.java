package co.edu.unbosque.marketplace.reserva;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import co.edu.unbosque.marketplace.usuario.security.JwtUtil;

public final class EscenarioReservaPrueba {

    public final UUID idAnfitrion = UUID.randomUUID();
    public final UUID idHuesped = UUID.randomUUID();
    public final UUID idOtroHuesped = UUID.randomUUID();
    public final UUID idAlojamiento = UUID.randomUUID();
    public final UUID idPolitica = UUID.randomUUID();
    public final UUID idPublicacion = UUID.randomUUID();
    public final UUID idConfiguracion = UUID.randomUUID();

    private final List<UUID> cotizaciones = new ArrayList<>();
    private final List<UUID> rolesCreados = new ArrayList<>();
    private final JdbcTemplate jdbc;
    private final MockMvc mockMvc;
    private final JwtUtil jwtUtil;
    private final UserDetailsService usuarios;

    private EscenarioReservaPrueba(JdbcTemplate jdbc, MockMvc mockMvc, JwtUtil jwtUtil, UserDetailsService usuarios) {
        this.jdbc = jdbc;
        this.mockMvc = mockMvc;
        this.jwtUtil = jwtUtil;
        this.usuarios = usuarios;
    }

    public static EscenarioReservaPrueba crear(
            JdbcTemplate jdbc,
            MockMvc mockMvc,
            JwtUtil jwtUtil,
            UserDetailsService usuarios,
            String modoAceptacion,
            String politica,
            int porcentajeDevolucion,
            Integer anticipacionHoras) {
        EscenarioReservaPrueba escenario = new EscenarioReservaPrueba(jdbc, mockMvc, jwtUtil, usuarios);
        escenario.preparar(modoAceptacion, politica, porcentajeDevolucion, anticipacionHoras);
        return escenario;
    }

    private void preparar(String modoAceptacion, String politica, int porcentajeDevolucion, Integer anticipacionHoras) {
        UUID rolHuesped = asegurarRol("HUESPED");
        UUID rolAnfitrion = asegurarRol("ANFITRION");
        crearUsuario(idAnfitrion, rolAnfitrion);
        crearUsuario(idHuesped, rolHuesped);
        crearUsuario(idOtroHuesped, rolHuesped);
        jdbc.update("""
                INSERT INTO alojamiento (id_alojamiento, id_anfitrion, ubicacion, ciudad, tipo_alojamiento, capacidad)
                VALUES (?, ?, 'Calle 1', 'Bogotá', 'Apartamento', 2)
                """, idAlojamiento, idAnfitrion);
        jdbc.update("""
                INSERT INTO periodo_disponibilidad (id_periodo, id_alojamiento, fecha_inicio, fecha_fin, habilitado)
                VALUES (?, ?, current_date, current_date + 120, true)
                """, UUID.randomUUID(), idAlojamiento);
        jdbc.update("""
                INSERT INTO politica_cancelacion
                    (id_politica, nombre, version, anticipacion_horas, porcentaje_devolucion, activa)
                VALUES (?, ?, ?, ?, ?, false)
                """, idPolitica, politica, ThreadLocalRandom.current().nextInt(100_000, Integer.MAX_VALUE),
                anticipacionHoras, porcentajeDevolucion);
        jdbc.update("""
                INSERT INTO publicacion (id_publicacion, id_alojamiento, id_politica, titulo, descripcion, reglas,
                    precio_por_noche, modo_aceptacion, estado)
                VALUES (?, ?, ?, 'Loft', 'Descripción', '', 100000, ?, 'ACTIVA')
                """, idPublicacion, idAlojamiento, idPolitica, modoAceptacion);
        jdbc.update("""
                INSERT INTO configuracion_tarifa (id_configuracion, id_administrador, porcentaje_servicio,
                    vigente_desde, vigente_hasta)
                VALUES (?, ?, 10, now() - interval '2 day', now() - interval '1 day')
                """, idConfiguracion, idAnfitrion);
    }

    public UUID nuevaCotizacion(int diasHastaEntrada, int noches) {
        UUID idCotizacion = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO cotizacion (id_cotizacion, id_publicacion, id_configuracion, fecha_entrada, fecha_salida,
                    cantidad_huespedes, precio_noche_aplicado, tarifa_limpieza, fecha_cotizacion)
                VALUES (?, ?, ?, current_date + ?, current_date + ?, 1, 100000, 0, now())
                """, idCotizacion, idPublicacion, idConfiguracion, diasHastaEntrada, diasHastaEntrada + noches);
        cotizaciones.add(idCotizacion);
        return idCotizacion;
    }

    public String token(UUID idUsuario) {
        return "Bearer " + jwtUtil.generateToken(usuarios.loadUserByUsername(correo(idUsuario)));
    }

    public MvcResult solicitar(UUID idUsuario, UUID idCotizacion, String clave) throws Exception {
        return mockMvc.perform(post("/reservas")
                        .header("Authorization", token(idUsuario))
                        .header("Idempotency-Key", clave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idCotizacion\":\"" + idCotizacion + "\"}"))
                .andReturn();
    }

    public MvcResult retener(UUID idUsuario, UUID idReserva) throws Exception {
        return mockMvc.perform(post("/reservas/{id}/retencion", idReserva)
                        .header("Authorization", token(idUsuario)))
                .andReturn();
    }

    public MvcResult pagar(UUID idUsuario, UUID idReserva, String clave) throws Exception {
        return mockMvc.perform(post("/reservas/{id}/pago-simulado", idReserva)
                        .header("Authorization", token(idUsuario))
                        .header("Idempotency-Key", clave))
                .andReturn();
    }

    public UUID solicitarReserva(UUID idUsuario, UUID idCotizacion) throws Exception {
        MvcResult respuesta = solicitar(idUsuario, idCotizacion, "solicitud-" + UUID.randomUUID());
        if (respuesta.getResponse().getStatus() != 201) {
            throw new IllegalStateException("No se pudo solicitar la reserva: "
                    + respuesta.getResponse().getContentAsString());
        }
        return UUID.fromString(JsonPath.read(respuesta.getResponse().getContentAsString(), "$.idReserva"));
    }

    public UUID reservaConfirmada(int diasHastaEntrada, int noches) throws Exception {
        UUID idReserva = solicitarReserva(idHuesped, nuevaCotizacion(diasHastaEntrada, noches));
        if (retener(idHuesped, idReserva).getResponse().getStatus() != 200) {
            throw new IllegalStateException("No se pudo retener la reserva.");
        }
        MvcResult pago = pagar(idHuesped, idReserva, "pago-" + UUID.randomUUID());
        if (!"CONFIRMADA".equals(JsonPath.read(pago.getResponse().getContentAsString(), "$.estadoReserva"))) {
            throw new IllegalStateException("No se pudo confirmar la reserva: "
                    + pago.getResponse().getContentAsString());
        }
        return idReserva;
    }

    public String estadoReserva(UUID idReserva) {
        return jdbc.queryForObject("SELECT estado FROM reserva WHERE id_reserva = ?", String.class, idReserva);
    }

    public int nochesOcupadas() {
        Integer total = jdbc.queryForObject(
                "SELECT count(*) FROM noche_reservada WHERE id_alojamiento = ?", Integer.class, idAlojamiento);
        return total == null ? 0 : total;
    }

    public void vencerRetencion(UUID idReserva) {
        jdbc.update("UPDATE reserva SET retencion_hasta = now() - interval '1 minute' WHERE id_reserva = ?",
                idReserva);
    }

    public void adelantarConciliacion(UUID idReserva) {
        jdbc.update("""
                UPDATE operacion_pendiente SET proxima_ejecucion = now() - interval '1 second'
                WHERE id_reserva = ? AND estado = 'PENDIENTE'
                """, idReserva);
    }

    public void limpiar() {
        List<UUID> reservas = new ArrayList<>();
        for (UUID idCotizacion : cotizaciones) {
            reservas.addAll(jdbc.queryForList(
                    "SELECT id_reserva FROM reserva WHERE id_cotizacion = ?", UUID.class, idCotizacion));
        }
        Object[] usuariosPrueba = {idAnfitrion, idHuesped, idOtroHuesped};
        jdbc.update("DELETE FROM notificacion WHERE id_usuario IN (?, ?, ?)", usuariosPrueba);
        for (UUID idReserva : reservas) {
            jdbc.update("DELETE FROM notificacion WHERE id_reserva = ?", idReserva);
            jdbc.update("DELETE FROM cancelacion WHERE id_reserva = ?", idReserva);
            jdbc.update("DELETE FROM operacion_pendiente WHERE id_reserva = ?", idReserva);
            jdbc.update("DELETE FROM pago WHERE id_reserva = ?", idReserva);
            jdbc.update("DELETE FROM reserva_noche WHERE id_reserva = ?", idReserva);
            jdbc.update("DELETE FROM bandeja_salida_auditoria WHERE id_entidad_afectada = ?", idReserva);
        }
        jdbc.update("DELETE FROM noche_reservada WHERE id_alojamiento = ?", idAlojamiento);
        for (UUID idReserva : reservas) {
            jdbc.update("DELETE FROM reserva WHERE id_reserva = ?", idReserva);
        }
        for (UUID idCotizacion : cotizaciones) {
            jdbc.update("DELETE FROM cotizacion WHERE id_cotizacion = ?", idCotizacion);
        }
        jdbc.update("DELETE FROM configuracion_tarifa WHERE id_configuracion = ?", idConfiguracion);
        jdbc.update("DELETE FROM publicacion WHERE id_publicacion = ?", idPublicacion);
        jdbc.update("DELETE FROM politica_cancelacion WHERE id_politica = ?", idPolitica);
        jdbc.update("DELETE FROM periodo_disponibilidad WHERE id_alojamiento = ?", idAlojamiento);
        jdbc.update("DELETE FROM alojamiento WHERE id_alojamiento = ?", idAlojamiento);
        jdbc.update("DELETE FROM bandeja_salida_auditoria WHERE id_usuario IN (?, ?, ?)", usuariosPrueba);
        jdbc.update("DELETE FROM usuario WHERE id_usuario IN (?, ?, ?)", usuariosPrueba);
        for (UUID idRol : rolesCreados) {
            jdbc.update("DELETE FROM rol r WHERE r.id_rol = ? AND NOT EXISTS "
                    + "(SELECT 1 FROM usuario u WHERE u.id_rol = r.id_rol)", idRol);
        }
    }

    private UUID asegurarRol(String nombre) {
        UUID idNuevo = UUID.randomUUID();
        if (jdbc.update("INSERT INTO rol (id_rol, nombre) VALUES (?, ?) ON CONFLICT (nombre) DO NOTHING",
                idNuevo, nombre) == 1) {
            rolesCreados.add(idNuevo);
        }
        return jdbc.queryForObject("SELECT id_rol FROM rol WHERE nombre = ?", UUID.class, nombre);
    }

    private void crearUsuario(UUID idUsuario, UUID idRol) {
        byte[] documento = new byte[32];
        ThreadLocalRandom.current().nextBytes(documento);
        String telefono = "+57" + ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
        jdbc.update("""
                INSERT INTO usuario (id_usuario, id_rol, nombre, apellido, correo, tipo_documento,
                    documento_cifrado, documento_nonce, documento_hash, telefono, contrasena_hash, estado,
                    correo_verificado)
                VALUES (?, ?, 'Prueba', 'Reserva', ?, 'CC', ?, ?, ?, ?, 'x', 'ACTIVA', true)
                """, idUsuario, idRol, correo(idUsuario), documento, new byte[12], documento, telefono);
    }

    private static String correo(UUID idUsuario) {
        return idUsuario + "@prueba.co";
    }
}
