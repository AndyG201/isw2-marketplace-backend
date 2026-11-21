package co.edu.unbosque.marketplace.notificacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.notificacion.application.dto.NotificacionResponse;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;
import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;

@ExtendWith(MockitoExtension.class)
class NotificacionConsultaServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionConsultaService service;

    private UUID idUsuario;
    private UUID idNotificacion;
    private UUID idReserva;
    private UUID idPublicacion;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaLectura;

    @BeforeEach
    void setUp() {
        idUsuario = UUID.randomUUID();
        idNotificacion = UUID.randomUUID();
        idReserva = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();

        fechaCreacion = OffsetDateTime.parse("2026-11-21T09:00:00-05:00");
        fechaLectura = OffsetDateTime.parse("2026-11-21T10:00:00-05:00");
    }

    private Notificacion crearNotificacion(
            UUID idNotificacion,
            UUID idUsuario,
            UUID idReserva,
            UUID idPublicacion,
            String tipo,
            String mensaje,
            OffsetDateTime fechaCreacion,
            OffsetDateTime fechaLectura) {

        Notificacion nueva = mock(Notificacion.class);

        when(nueva.getIdNotificacion()).thenReturn(idNotificacion);
        when(nueva.getIdUsuario()).thenReturn(idUsuario);
        when(nueva.getIdReserva()).thenReturn(idReserva);
        when(nueva.getIdPublicacion()).thenReturn(idPublicacion);
        when(nueva.getTipo()).thenReturn(tipo);
        when(nueva.getMensaje()).thenReturn(mensaje);
        when(nueva.getFechaCreacion()).thenReturn(fechaCreacion);
        when(nueva.getFechaLectura()).thenReturn(fechaLectura);

        return nueva;
    }

    @Test
    void consultarMisNotificacionesDebeRechazarUsuarioNulo() {

        SesionRequeridaException excepcion = assertThrows(
                SesionRequeridaException.class,
                () -> service.consultarMisNotificaciones(null)
        );

        assertEquals(
                "No se pudo identificar al usuario.",
                excepcion.getMessage()
        );

        verifyNoInteractions(notificacionRepository);
    }

    @Test
    void consultarMisNotificacionesDebeRetornarLasNotificacionesDelUsuario() {

        Notificacion notificacion = crearNotificacion(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                "RESERVA_CONFIRMADA",
                "Tu reserva ha sido confirmada.",
                fechaCreacion,
                fechaLectura
        );

        when(notificacionRepository
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario))
                .thenReturn(List.of(notificacion));

        List<NotificacionResponse> respuestas =
                service.consultarMisNotificaciones(idUsuario);

        assertEquals(1, respuestas.size());

        NotificacionResponse respuesta = respuestas.get(0);

        assertAll(
                () -> assertEquals(idNotificacion, respuesta.idNotificacion()),
                () -> assertEquals(idUsuario, respuesta.idUsuario()),
                () -> assertEquals(idReserva, respuesta.idReserva()),
                () -> assertEquals(idPublicacion, respuesta.idPublicacion()),
                () -> assertEquals("RESERVA_CONFIRMADA", respuesta.tipo()),
                () -> assertEquals(
                        "Tu reserva ha sido confirmada.",
                        respuesta.mensaje()
                ),
                () -> assertEquals(fechaCreacion, respuesta.fechaCreacion()),
                () -> assertEquals(fechaLectura, respuesta.fechaLectura())
        );

        verify(notificacionRepository)
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);
    }

    @Test
    void consultarMisNotificacionesDebeRetornarListaVaciaCuandoNoHayResultados() {

        when(notificacionRepository
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario))
                .thenReturn(List.of());

        List<NotificacionResponse> respuestas =
                service.consultarMisNotificaciones(idUsuario);

        assertNotNull(respuestas);
        assertTrue(respuestas.isEmpty());

        verify(notificacionRepository)
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);
    }

    @Test
    void consultarMisNotificacionesDebeConvertirVariasNotificaciones() {

        Notificacion primera = crearNotificacion(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                "RESERVA_CONFIRMADA",
                "Tu reserva ha sido confirmada.",
                fechaCreacion,
                fechaLectura
        );

        Notificacion segunda = crearNotificacion(
                UUID.randomUUID(),
                idUsuario,
                null,
                null,
                "PUBLICACION_ACTUALIZADA",
                "Una publicación fue actualizada.",
                fechaCreacion.plusHours(1),
                null
        );

        when(notificacionRepository
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario))
                .thenReturn(List.of(primera, segunda));

        List<NotificacionResponse> respuestas =
                service.consultarMisNotificaciones(idUsuario);

        assertEquals(2, respuestas.size());

        assertAll(
                () -> assertEquals("RESERVA_CONFIRMADA", respuestas.get(0).tipo()),
                () -> assertEquals("PUBLICACION_ACTUALIZADA", respuestas.get(1).tipo()),
                () -> assertNull(respuestas.get(1).idReserva()),
                () -> assertNull(respuestas.get(1).idPublicacion()),
                () -> assertNull(respuestas.get(1).fechaLectura())
        );
    }

    @Test
    void consultarMisNotificacionesDebeConservarElOrdenDelRepositorio() {

        Notificacion primera = crearNotificacion(
                UUID.randomUUID(),
                idUsuario,
                null,
                null,
                "NUEVA_NOTIFICACION",
                "Segunda notificación.",
                fechaCreacion.plusHours(1),
                null
        );

        Notificacion segunda = crearNotificacion(
                idNotificacion,
                idUsuario,
                idReserva,
                idPublicacion,
                "RESERVA_CONFIRMADA",
                "Tu reserva ha sido confirmada.",
                fechaCreacion,
                fechaLectura
        );

        when(notificacionRepository
                .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario))
                .thenReturn(List.of(primera, segunda));

        List<NotificacionResponse> respuestas =
                service.consultarMisNotificaciones(idUsuario);

        assertEquals(2, respuestas.size());
        assertEquals("NUEVA_NOTIFICACION", respuestas.get(0).tipo());
        assertEquals("RESERVA_CONFIRMADA", respuestas.get(1).tipo());
    }
}
