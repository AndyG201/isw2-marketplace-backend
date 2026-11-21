package co.edu.unbosque.marketplace.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import co.edu.unbosque.marketplace.reserva.application.port.ReservaRepository;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@SpringBootTest
@ActiveProfiles("test")
class NotificacionCambioEstadoReservaTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transaccion;

    @Autowired
    private ReservaRepository reservas;

    private final UUID idRol = UUID.randomUUID();
    private final UUID idHuesped = UUID.randomUUID();
    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();
    private final UUID idConfiguracion = UUID.randomUUID();
    private final UUID idCotizacion = UUID.randomUUID();
    private final UUID idReserva = UUID.randomUUID();

    @BeforeEach
    void prepararReservaPendienteDePago() {
        jdbc.update("INSERT INTO rol (id_rol, nombre) VALUES (?, ?)", idRol, "PRUEBA_" + idRol);
        crearUsuario(idHuesped);
        crearUsuario(idAnfitrion);
        jdbc.update("""
                INSERT INTO alojamiento (id_alojamiento, id_anfitrion, ubicacion, ciudad, tipo_alojamiento, capacidad)
                VALUES (?, ?, 'Calle 1', 'Bogotá', 'Apartamento', 2)
                """, idAlojamiento, idAnfitrion);
        jdbc.update("""
                INSERT INTO politica_cancelacion
                    (id_politica, nombre, version, porcentaje_devolucion, activa)
                VALUES (?, ?, 1, 100, false)
                """, idPolitica, "PRUEBA_" + idPolitica);
        jdbc.update("""
                INSERT INTO publicacion (id_publicacion, id_alojamiento, id_politica, titulo, descripcion, reglas,
                    precio_por_noche, modo_aceptacion, estado)
                VALUES (?, ?, ?, 'Loft', 'Descripción', '', 100000, 'INMEDIATA', 'ACTIVA')
                """, idPublicacion, idAlojamiento, idPolitica);
        jdbc.update("""
                INSERT INTO configuracion_tarifa (id_configuracion, id_administrador, porcentaje_servicio,
                    vigente_desde, vigente_hasta)
                VALUES (?, ?, 10, now() - interval '2 day', now() - interval '1 day')
                """, idConfiguracion, idAnfitrion);
        jdbc.update("""
                INSERT INTO cotizacion (id_cotizacion, id_publicacion, id_configuracion, fecha_entrada, fecha_salida,
                    cantidad_huespedes, precio_noche_aplicado, tarifa_limpieza, fecha_cotizacion)
                VALUES (?, ?, ?, current_date + 30, current_date + 32, 1, 100000, 0, now())
                """, idCotizacion, idPublicacion, idConfiguracion);
        jdbc.update("""
                INSERT INTO reserva (id_reserva, id_huesped, id_cotizacion, id_politica_aplicada, modo_aceptacion,
                    estado, clave_idempotencia, requiere_revision_manual, fecha_solicitud)
                VALUES (?, ?, ?, ?, 'INMEDIATA', 'PENDIENTE_PAGO', ?, false, now())
                """, idReserva, idHuesped, idCotizacion, idPolitica, "prueba-" + idReserva);
    }

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM notificacion WHERE id_reserva = ?", idReserva);
        jdbc.update("DELETE FROM reserva WHERE id_reserva = ?", idReserva);
        jdbc.update("DELETE FROM cotizacion WHERE id_cotizacion = ?", idCotizacion);
        jdbc.update("DELETE FROM configuracion_tarifa WHERE id_configuracion = ?", idConfiguracion);
        jdbc.update("DELETE FROM publicacion WHERE id_publicacion = ?", idPublicacion);
        jdbc.update("DELETE FROM politica_cancelacion WHERE id_politica = ?", idPolitica);
        jdbc.update("DELETE FROM alojamiento WHERE id_alojamiento = ?", idAlojamiento);
        jdbc.update("DELETE FROM usuario WHERE id_usuario IN (?, ?)", idHuesped, idAnfitrion);
        jdbc.update("DELETE FROM rol WHERE id_rol = ?", idRol);
    }

    @Test
    void notificaAlHuespedYAlAnfitrionAunqueElCambioSeGuardeAlConfirmarLaTransaccion() {
        transaccion.executeWithoutResult(estado -> {
            Reserva reserva = reservas.findById(idReserva).orElseThrow();
            reserva.setEstado("CONFIRMADA");
            reservas.save(reserva);
        });

        List<UUID> destinatarios = destinatarios();
        assertEquals(2, destinatarios.size());
        assertEquals(Set.of(idHuesped, idAnfitrion), Set.copyOf(destinatarios));
    }

    @Test
    void noDuplicaLaNotificacionCuandoElCambioSeGuardaDeInmediato() {
        transaccion.executeWithoutResult(estado -> {
            Reserva reserva = reservas.findById(idReserva).orElseThrow();
            reserva.setEstado("CONFIRMADA");
            reservas.saveAndFlush(reserva);
        });

        assertEquals(2, destinatarios().size());
    }

    @Test
    void noNotificaUnCambioQueSeRevierte() {
        assertThrows(IllegalStateException.class, () -> transaccion.executeWithoutResult(estado -> {
            Reserva reserva = reservas.findById(idReserva).orElseThrow();
            reserva.setEstado("CONFIRMADA");
            reservas.saveAndFlush(reserva);
            throw new IllegalStateException("Falla después de guardar");
        }));

        assertEquals(0, destinatarios().size());
    }

    private List<UUID> destinatarios() {
        return jdbc.queryForList(
                "SELECT id_usuario FROM notificacion WHERE id_reserva = ? AND tipo = 'RESERVA_ESTADO_CAMBIADO'",
                UUID.class,
                idReserva);
    }

    private void crearUsuario(UUID idUsuario) {
        byte[] documento = new byte[32];
        ThreadLocalRandom.current().nextBytes(documento);
        String telefono = "+57" + ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
        jdbc.update("""
                INSERT INTO usuario (id_usuario, id_rol, nombre, apellido, correo, tipo_documento,
                    documento_cifrado, documento_nonce, documento_hash, telefono, contrasena_hash, estado)
                VALUES (?, ?, 'Prueba', 'Notificación', ?, 'CC', ?, ?, ?, ?, 'x', 'ACTIVA')
                """, idUsuario, idRol, idUsuario + "@prueba.co", documento, new byte[12], documento, telefono);
    }
}
