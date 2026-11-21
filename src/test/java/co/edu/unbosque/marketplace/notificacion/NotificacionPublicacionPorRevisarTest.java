package co.edu.unbosque.marketplace.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.List;
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

import co.edu.unbosque.marketplace.publicacion.application.CambiosPublicacionService;
import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@SpringBootTest
@ActiveProfiles("test")
class NotificacionPublicacionPorRevisarTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TransactionTemplate transaccion;

    @Autowired
    private PublicacionRepository publicaciones;

    @Autowired
    private CambiosPublicacionService cambios;

    private final UUID idRolPrueba = UUID.randomUUID();
    private UUID idRolAdministrador;
    private boolean rolAdministradorCreado;
    private final UUID idAdministrador = UUID.randomUUID();
    private final UUID idAnfitrion = UUID.randomUUID();
    private final UUID idAlojamiento = UUID.randomUUID();
    private final UUID idPolitica = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();

    @BeforeEach
    void preparar() {
        List<UUID> existentes = jdbc.queryForList(
                "SELECT id_rol FROM rol WHERE nombre = 'ADMINISTRADOR'", UUID.class);
        if (existentes.isEmpty()) {
            idRolAdministrador = UUID.randomUUID();
            rolAdministradorCreado = true;
            jdbc.update("INSERT INTO rol (id_rol, nombre) VALUES (?, 'ADMINISTRADOR')", idRolAdministrador);
        } else {
            idRolAdministrador = existentes.get(0);
        }
        jdbc.update("INSERT INTO rol (id_rol, nombre) VALUES (?, ?)", idRolPrueba, "PRUEBA_" + idRolPrueba);
        crearUsuario(idAdministrador, idRolAdministrador);
        crearUsuario(idAnfitrion, idRolPrueba);
        jdbc.update("""
                INSERT INTO alojamiento (id_alojamiento, id_anfitrion, ubicacion, ciudad, tipo_alojamiento, capacidad)
                VALUES (?, ?, 'Calle 1', 'Bogotá', 'Apartamento', 2)
                """, idAlojamiento, idAnfitrion);
        jdbc.update("""
                INSERT INTO politica_cancelacion (id_politica, nombre, version, porcentaje_devolucion, activa)
                VALUES (?, ?, 1, 100, false)
                """, idPolitica, "PRUEBA_" + idPolitica);
        jdbc.update("""
                INSERT INTO publicacion (id_publicacion, id_alojamiento, id_politica, titulo, descripcion, reglas,
                    precio_por_noche, modo_aceptacion, estado)
                VALUES (?, ?, ?, 'Loft', 'Descripción', '', 100000, 'INMEDIATA', 'ACTIVA')
                """, idPublicacion, idAlojamiento, idPolitica);
    }

    @AfterEach
    void limpiar() {
        jdbc.update("DELETE FROM notificacion WHERE id_publicacion = ?", idPublicacion);
        jdbc.update("DELETE FROM solicitud_cambio_publicacion WHERE id_publicacion = ?", idPublicacion);
        jdbc.update("DELETE FROM publicacion WHERE id_publicacion = ?", idPublicacion);
        jdbc.update("DELETE FROM politica_cancelacion WHERE id_politica = ?", idPolitica);
        jdbc.update("DELETE FROM alojamiento WHERE id_alojamiento = ?", idAlojamiento);
        jdbc.update("DELETE FROM usuario WHERE id_usuario IN (?, ?)", idAdministrador, idAnfitrion);
        jdbc.update("DELETE FROM rol WHERE id_rol = ?", idRolPrueba);
        if (rolAdministradorCreado) {
            jdbc.update("DELETE FROM rol WHERE id_rol = ?", idRolAdministrador);
        }
    }

    @Test
    void avisaALosAdministradoresCuandoUnaPublicacionEditadaPasaARevision() {
        transaccion.executeWithoutResult(estado -> {
            Publicacion publicacion = publicaciones.findById(idPublicacion).orElseThrow();
            publicacion.setEstado("PENDIENTE_REVISION");
            publicaciones.save(publicacion);
            cambios.registrarSolicitud(idPublicacion, idAnfitrion, "Cambié las fotos",
                    List.of("Fotos"), "ACTIVA", OffsetDateTime.now());
        });

        List<String> mensajes = jdbc.queryForList("""
                SELECT mensaje FROM notificacion
                WHERE id_publicacion = ? AND id_usuario = ? AND tipo = 'PUBLICACION_POR_REVISAR'
                """, String.class, idPublicacion, idAdministrador);
        assertEquals(List.of("Un anfitrión editó una publicación aprobada y espera tu revisión."), mensajes);
        assertEquals(1, jdbc.queryForObject(
                "SELECT count(*) FROM notificacion WHERE id_publicacion = ? AND id_usuario = ?",
                Integer.class, idPublicacion, idAnfitrion));

        var pendiente = cambios.pendiente(idPublicacion).orElseThrow();
        assertEquals(List.of("Fotos"), pendiente.campos());
        assertEquals("ACTIVA", pendiente.estadoAnterior());
        assertTrue(cambios.resolver(idPublicacion, OffsetDateTime.now()).isPresent());
        assertTrue(cambios.pendiente(idPublicacion).isEmpty());
    }

    @Test
    void avisaQueHayUnaPublicacionNuevaCuandoSaleDeBorrador() {
        jdbc.update("UPDATE publicacion SET estado = 'BORRADOR' WHERE id_publicacion = ?", idPublicacion);
        transaccion.executeWithoutResult(estado -> {
            Publicacion publicacion = publicaciones.findById(idPublicacion).orElseThrow();
            publicacion.setEstado("PENDIENTE_REVISION");
            publicaciones.save(publicacion);
        });

        assertEquals(List.of("Hay una publicación nueva esperando tu revisión."), jdbc.queryForList("""
                SELECT mensaje FROM notificacion
                WHERE id_publicacion = ? AND id_usuario = ? AND tipo = 'PUBLICACION_POR_REVISAR'
                """, String.class, idPublicacion, idAdministrador));
    }

    private void crearUsuario(UUID idUsuario, UUID idRol) {
        byte[] documento = new byte[32];
        ThreadLocalRandom.current().nextBytes(documento);
        String telefono = "+57" + ThreadLocalRandom.current().nextLong(1_000_000_000L, 9_999_999_999L);
        jdbc.update("""
                INSERT INTO usuario (id_usuario, id_rol, nombre, apellido, correo, tipo_documento,
                    documento_cifrado, documento_nonce, documento_hash, telefono, contrasena_hash, estado)
                VALUES (?, ?, 'Prueba', 'Revisión', ?, 'CC', ?, ?, ?, ?, 'x', 'ACTIVA')
                """, idUsuario, idRol, idUsuario + "@prueba.co", documento, new byte[12], documento, telefono);
    }
}
