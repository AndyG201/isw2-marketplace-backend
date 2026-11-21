package co.edu.unbosque.marketplace.notificacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificacionTest {

    private Notificacion notificacion;
    private UUID idNotificacion;
    private UUID idUsuario;
    private UUID idReserva;
    private UUID idPublicacion;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaLectura;

    @BeforeEach
    void setUp() {
        idNotificacion = UUID.randomUUID();
        idUsuario = UUID.randomUUID();
        idReserva = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();

        fechaCreacion = OffsetDateTime.parse("2026-11-21T09:00:00-05:00");
        fechaLectura = OffsetDateTime.parse("2026-11-21T10:00:00-05:00");

        notificacion = new Notificacion(
            idNotificacion,
            idUsuario,
            idReserva,
            idPublicacion,
            "RESERVA_CONFIRMADA",
            "Tu reserva ha sido confirmada.",
            fechaCreacion,
            fechaLectura
        );
    }

    @Test
    void constructorVacioDebeCrearObjeto() {
        Notificacion nueva = new Notificacion();

        assertNotNull(nueva);
        assertNull(nueva.getIdNotificacion());
        assertNull(nueva.getIdUsuario());
        assertNull(nueva.getIdReserva());
        assertNull(nueva.getIdPublicacion());
        assertNull(nueva.getTipo());
        assertNull(nueva.getMensaje());
        assertNull(nueva.getFechaCreacion());
        assertNull(nueva.getFechaLectura());
    }

    @Test
    void constructorCompletoDebeAsignarTodosLosAtributos() {
        assertAll(
            () -> assertEquals(idNotificacion, notificacion.getIdNotificacion()),
            () -> assertEquals(idUsuario, notificacion.getIdUsuario()),
            () -> assertEquals(idReserva, notificacion.getIdReserva()),
            () -> assertEquals(idPublicacion, notificacion.getIdPublicacion()),
            () -> assertEquals("RESERVA_CONFIRMADA", notificacion.getTipo()),
            () -> assertEquals("Tu reserva ha sido confirmada.", notificacion.getMensaje()),
            () -> assertEquals(fechaCreacion, notificacion.getFechaCreacion()),
            () -> assertEquals(fechaLectura, notificacion.getFechaLectura())
        );
    }

    @Test
    void debeModificarTodosLosAtributosConSusSetters() {
        UUID nuevoIdNotificacion = UUID.randomUUID();
        UUID nuevoIdUsuario = UUID.randomUUID();
        UUID nuevoIdReserva = UUID.randomUUID();
        UUID nuevoIdPublicacion = UUID.randomUUID();

        OffsetDateTime nuevaFechaCreacion = fechaCreacion.plusDays(1);
        OffsetDateTime nuevaFechaLectura = fechaLectura.plusDays(1);

        notificacion.setIdNotificacion(nuevoIdNotificacion);
        notificacion.setIdUsuario(nuevoIdUsuario);
        notificacion.setIdReserva(nuevoIdReserva);
        notificacion.setIdPublicacion(nuevoIdPublicacion);
        notificacion.setTipo("PUBLICACION_DESBLOQUEADA");
        notificacion.setMensaje("Tu publicación ha sido desbloqueada.");
        notificacion.setFechaCreacion(nuevaFechaCreacion);
        notificacion.setFechaLectura(nuevaFechaLectura);

        assertAll(
            () -> assertEquals(nuevoIdNotificacion, notificacion.getIdNotificacion()),
            () -> assertEquals(nuevoIdUsuario, notificacion.getIdUsuario()),
            () -> assertEquals(nuevoIdReserva, notificacion.getIdReserva()),
            () -> assertEquals(nuevoIdPublicacion, notificacion.getIdPublicacion()),
            () -> assertEquals("PUBLICACION_DESBLOQUEADA", notificacion.getTipo()),
            () -> assertEquals("Tu publicación ha sido desbloqueada.", notificacion.getMensaje()),
            () -> assertEquals(nuevaFechaCreacion, notificacion.getFechaCreacion()),
            () -> assertEquals(nuevaFechaLectura, notificacion.getFechaLectura())
        );
    }

    @Test
    void debePermitirValoresNulosEnLosAtributosOpcionales() {
        notificacion.setIdReserva(null);
        notificacion.setIdPublicacion(null);
        notificacion.setFechaLectura(null);

        assertAll(
            () -> assertNull(notificacion.getIdReserva()),
            () -> assertNull(notificacion.getIdPublicacion()),
            () -> assertNull(notificacion.getFechaLectura())
        );
    }

    @Test
    void debePermitirModificarElEstadoDeLectura() {
        assertNotNull(notificacion.getFechaLectura());

        notificacion.setFechaLectura(null);

        assertNull(notificacion.getFechaLectura());

        OffsetDateTime fechaLecturaNueva = fechaCreacion.plusHours(2);
        notificacion.setFechaLectura(fechaLecturaNueva);

        assertEquals(fechaLecturaNueva, notificacion.getFechaLectura());
    }

    @Test
    void debeConservarElDesfaseHorarioDeLasFechas() {
        OffsetDateTime fechaConDesfase =
            OffsetDateTime.parse("2026-11-21T15:30:00+02:00");

        notificacion.setFechaCreacion(fechaConDesfase);

        assertEquals(fechaConDesfase, notificacion.getFechaCreacion());
        assertEquals("+02:00", notificacion.getFechaCreacion().getOffset().toString());
    }

    @Test
    void debePermitirCrearNotificacionSinReservaNiPublicacion() {
        Notificacion nueva = new Notificacion(
            UUID.randomUUID(),
            idUsuario,
            null,
            null,
            "PUBLICACION_DESBLOQUEADA",
            "Tu publicación ha sido desbloqueada.",
            fechaCreacion,
            null
        );

        assertAll(
            () -> assertNull(nueva.getIdReserva()),
            () -> assertNull(nueva.getIdPublicacion()),
            () -> assertNull(nueva.getFechaLectura()),
            () -> assertEquals("PUBLICACION_DESBLOQUEADA", nueva.getTipo())
        );
    }
}
