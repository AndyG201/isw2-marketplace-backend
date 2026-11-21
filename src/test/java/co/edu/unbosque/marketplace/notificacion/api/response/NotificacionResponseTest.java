package co.edu.unbosque.marketplace.notificacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificacionResponseTest {

    private UUID idNotificacion;
    private UUID idUsuario;
    private UUID idReserva;
    private UUID idPublicacion;
    private String tipo;
    private String mensaje;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaLectura;

    @BeforeEach
    void setUp() {
        idNotificacion = UUID.randomUUID();
        idUsuario = UUID.randomUUID();
        idReserva = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        tipo = "RESERVA_CONFIRMADA";
        mensaje = "La reserva ha sido confirmada.";
        fechaCreacion = OffsetDateTime.parse(
                "2026-11-20T10:30:00-05:00"
        );
        fechaLectura = OffsetDateTime.parse(
                "2026-11-20T11:00:00-05:00"
        );
    }

    @Test
    void constructorYObtenerAtributos() {
        NotificacionResponse respuesta = new NotificacionResponse(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                tipo,
                mensaje,
                fechaCreacion,
                fechaLectura
        );

        assertEquals(idNotificacion, respuesta.idNotificacion());
        assertEquals(idUsuario, respuesta.idUsuario());
        assertEquals(idReserva, respuesta.idReserva());
        assertEquals(idPublicacion, respuesta.idPublicacion());
        assertEquals(tipo, respuesta.tipo());
        assertEquals(mensaje, respuesta.mensaje());
        assertEquals(fechaCreacion, respuesta.fechaCreacion());
        assertEquals(fechaLectura, respuesta.fechaLectura());
    }

    @Test
    void constructorConFechaLecturaNula() {
        NotificacionResponse respuesta = new NotificacionResponse(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                tipo,
                mensaje,
                fechaCreacion,
                null
        );

        assertNull(respuesta.fechaLectura());
        assertEquals(fechaCreacion, respuesta.fechaCreacion());
    }

    @Test
    void constructorConIdentificadoresOpcionalesNulos() {
        NotificacionResponse respuesta = new NotificacionResponse(
                idNotificacion,
                idUsuario,
                null,
                null,
                tipo,
                mensaje,
                fechaCreacion,
                null
        );

        assertNull(respuesta.idReserva());
        assertNull(respuesta.idPublicacion());
    }

    @Test
    void compararDosNotificacionesIguales() {
        NotificacionResponse respuesta1 = new NotificacionResponse(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                tipo,
                mensaje,
                fechaCreacion,
                fechaLectura
        );

        NotificacionResponse respuesta2 = new NotificacionResponse(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                tipo,
                mensaje,
                fechaCreacion,
                fechaLectura
        );

        assertEquals(respuesta1, respuesta2);
        assertEquals(respuesta1.hashCode(), respuesta2.hashCode());
    }

    @Test
    void compararNotificacionesDiferentes() {
        NotificacionResponse respuesta1 = new NotificacionResponse(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                tipo,
                mensaje,
                fechaCreacion,
                fechaLectura
        );

        NotificacionResponse respuesta2 = new NotificacionResponse(
                UUID.randomUUID(),
                idUsuario,
                idReserva,
                idPublicacion,
                tipo,
                mensaje,
                fechaCreacion,
                fechaLectura
        );

        assertNotEquals(respuesta1, respuesta2);
    }

    @Test
    void verificarToString() {
        NotificacionResponse respuesta = new NotificacionResponse(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                tipo,
                mensaje,
                fechaCreacion,
                fechaLectura
        );

        assertTrue(respuesta.toString().contains(tipo));
        assertTrue(respuesta.toString().contains(mensaje));
    }
}
