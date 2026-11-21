package co.edu.unbosque.marketplace.notificacion.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.ModoAceptacionActualizadoEvent;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDesbloqueadaEvent;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService service;

    private UUID idPublicacion;
    private UUID idAnfitrion;
    private OffsetDateTime fechaEvento;

    @BeforeEach
    void setUp() {
        idPublicacion = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();
        fechaEvento = OffsetDateTime.parse("2026-11-21T10:00:00-05:00");
    }

    private Notificacion capturarNotificacion() {
        ArgumentCaptor<Notificacion> captor =
            ArgumentCaptor.forClass(Notificacion.class);

        verify(notificacionRepository).save(captor.capture());

        return captor.getValue();
    }

    @Test
    void registrarModoAceptacionActualizadoDebeGuardarNotificacion() {
        ModoAceptacionActualizadoEvent evento =
            new ModoAceptacionActualizadoEvent(
                idPublicacion,
                idAnfitrion,
                "Apartamento moderno",
                "INMEDIATA",
                "APROBACION",
                fechaEvento
            );

        service.registrarModoAceptacionActualizado(evento);

        Notificacion notificacion = capturarNotificacion();

        assertAll(
            () -> assertNotNull(notificacion.getIdNotificacion()),
            () -> assertEquals(idAnfitrion, notificacion.getIdUsuario()),
            () -> assertNull(notificacion.getIdReserva()),
            () -> assertEquals(idPublicacion, notificacion.getIdPublicacion()),
            () -> assertEquals("MODO_ACEPTACION_ACTUALIZADO", notificacion.getTipo()),
            () -> assertEquals(fechaEvento, notificacion.getFechaCreacion()),
            () -> assertNull(notificacion.getFechaLectura()),
            () -> assertEquals(
                "El administrador actualizó el modo de aceptación de reservas de tu publicación \"Apartamento moderno\". El modo anterior era aceptación inmediata y el nuevo modo es sujeta a aprobación del anfitrión.",
                notificacion.getMensaje()
            )
        );
    }

    @Test
    void registrarModoAceptacionActualizadoDebeUsarTituloPredeterminado() {
        ModoAceptacionActualizadoEvent evento =
            new ModoAceptacionActualizadoEvent(
                idPublicacion,
                idAnfitrion,
                "   ",
                "OTRO",
                "INMEDIATA",
                fechaEvento
            );

        service.registrarModoAceptacionActualizado(evento);

        Notificacion notificacion = capturarNotificacion();

        assertTrue(notificacion.getMensaje().contains("de tu publicación tu publicación."));
        assertTrue(notificacion.getMensaje().contains("sin configuración previa"));
        assertTrue(notificacion.getMensaje().contains("aceptación inmediata"));
    }

    @Test
    void registrarModoAceptacionActualizadoDebeLimpiarTitulo() {
        ModoAceptacionActualizadoEvent evento =
            new ModoAceptacionActualizadoEvent(
                idPublicacion,
                idAnfitrion,
                "  Casa campestre  ",
                "APROBACION",
                "INMEDIATA",
                fechaEvento
            );

        service.registrarModoAceptacionActualizado(evento);

        Notificacion notificacion = capturarNotificacion();

        assertTrue(notificacion.getMensaje().contains("\"Casa campestre\""));
        assertFalse(notificacion.getMensaje().contains("  Casa campestre  "));
    }

    @Test
    void registrarModoAceptacionActualizadoDebeValidarDatosObligatorios() {
        ModoAceptacionActualizadoEvent eventoNulo = null;

        ModoAceptacionActualizadoEvent sinAnfitrion =
            new ModoAceptacionActualizadoEvent(
                idPublicacion, null, "Casa", "INMEDIATA", "APROBACION", fechaEvento
            );

        ModoAceptacionActualizadoEvent sinPublicacion =
            new ModoAceptacionActualizadoEvent(
                null, idAnfitrion, "Casa", "INMEDIATA", "APROBACION", fechaEvento
            );

        ModoAceptacionActualizadoEvent sinModoNuevo =
            new ModoAceptacionActualizadoEvent(
                idPublicacion, idAnfitrion, "Casa", "INMEDIATA", null, fechaEvento
            );

        ModoAceptacionActualizadoEvent sinFecha =
            new ModoAceptacionActualizadoEvent(
                idPublicacion, idAnfitrion, "Casa", "INMEDIATA", "APROBACION", null
            );

        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarModoAceptacionActualizado(eventoNulo)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarModoAceptacionActualizado(sinAnfitrion)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarModoAceptacionActualizado(sinPublicacion)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarModoAceptacionActualizado(sinModoNuevo)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarModoAceptacionActualizado(sinFecha))
        );

        verifyNoInteractions(notificacionRepository);
    }

    @Test
    void registrarPublicacionDesbloqueadaDebeGuardarNotificacion() {
        PublicacionDesbloqueadaEvent evento =
            new PublicacionDesbloqueadaEvent(
                idPublicacion,
                idAnfitrion,
                "Apartamento moderno",
                "Se corrigió la información requerida.",
                fechaEvento
            );

        service.registrarPublicacionDesbloqueada(evento);

        Notificacion notificacion = capturarNotificacion();

        assertAll(
            () -> assertNotNull(notificacion.getIdNotificacion()),
            () -> assertEquals(idAnfitrion, notificacion.getIdUsuario()),
            () -> assertNull(notificacion.getIdReserva()),
            () -> assertEquals(idPublicacion, notificacion.getIdPublicacion()),
            () -> assertEquals("PUBLICACION_DESBLOQUEADA", notificacion.getTipo()),
            () -> assertEquals(fechaEvento, notificacion.getFechaCreacion()),
            () -> assertNull(notificacion.getFechaLectura()),
            () -> assertEquals(
                "Tu publicación \"Apartamento moderno\" ha sido desbloqueada por el administrador. Su estado actual es PAUSADA y no está disponible para nuevas reservas. Motivo del desbloqueo: Se corrigió la información requerida.",
                notificacion.getMensaje()
            )
        );
    }

    @Test
    void registrarPublicacionDesbloqueadaDebeUsarValoresPredeterminados() {
        PublicacionDesbloqueadaEvent evento =
            new PublicacionDesbloqueadaEvent(
                idPublicacion,
                idAnfitrion,
                null,
                null,
                fechaEvento
            );

        service.registrarPublicacionDesbloqueada(evento);

        Notificacion notificacion = capturarNotificacion();

        assertTrue(notificacion.getMensaje().contains("tu publicación"));
        assertTrue(notificacion.getMensaje().contains(
            "No se proporcionó información adicional."
        ));
    }

    @Test
    void registrarPublicacionDesbloqueadaDebeLimpiarTituloYMotivo() {
        PublicacionDesbloqueadaEvent evento =
            new PublicacionDesbloqueadaEvent(
                idPublicacion,
                idAnfitrion,
                "  Casa campestre  ",
                "  Información corregida  ",
                fechaEvento
            );

        service.registrarPublicacionDesbloqueada(evento);

        Notificacion notificacion = capturarNotificacion();

        assertTrue(notificacion.getMensaje().contains("\"Casa campestre\""));
        assertTrue(notificacion.getMensaje().contains("Motivo del desbloqueo: Información corregida."));
        assertFalse(notificacion.getMensaje().contains("  Información corregida  "));
    }

    @Test
    void registrarPublicacionDesbloqueadaDebeValidarDatosObligatorios() {
        PublicacionDesbloqueadaEvent eventoNulo = null;

        PublicacionDesbloqueadaEvent sinAnfitrion =
            new PublicacionDesbloqueadaEvent(
                idPublicacion, null, "Casa", "Motivo", fechaEvento
            );

        PublicacionDesbloqueadaEvent sinPublicacion =
            new PublicacionDesbloqueadaEvent(
                null, idAnfitrion, "Casa", "Motivo", fechaEvento
            );

        PublicacionDesbloqueadaEvent sinFecha =
            new PublicacionDesbloqueadaEvent(
                idPublicacion, idAnfitrion, "Casa", "Motivo", null
            );

        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarPublicacionDesbloqueada(eventoNulo)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarPublicacionDesbloqueada(sinAnfitrion)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarPublicacionDesbloqueada(sinPublicacion)),
            () -> assertThrows(IllegalArgumentException.class,
                () -> service.registrarPublicacionDesbloqueada(sinFecha))
        );

        verifyNoInteractions(notificacionRepository);
    }

    @Test
    void ambosMetodosDebenGuardarUnaNotificacionPorLlamada() {
        ModoAceptacionActualizadoEvent eventoModo =
            new ModoAceptacionActualizadoEvent(
                idPublicacion,
                idAnfitrion,
                "Apartamento",
                "INMEDIATA",
                "APROBACION",
                fechaEvento
            );

        PublicacionDesbloqueadaEvent eventoDesbloqueo =
            new PublicacionDesbloqueadaEvent(
                idPublicacion,
                idAnfitrion,
                "Apartamento",
                "Revisión completada",
                fechaEvento
            );

        service.registrarModoAceptacionActualizado(eventoModo);
        service.registrarPublicacionDesbloqueada(eventoDesbloqueo);

        verify(notificacionRepository, times(2)).save(any(Notificacion.class));
    }
}
