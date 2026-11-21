package co.edu.unbosque.marketplace.notificacion.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.notificacion.application.port.NotificacionRepository;

class NotificacionDatosObligatoriosTest {

    private final NotificacionRepository repositorio = mock(NotificacionRepository.class);

    @Test
    void crearNotificacionExigeLosDatos() {
        NotificacionUsuarioService servicio = new NotificacionUsuarioService(repositorio);

        assertThrows(DatosInvalidosException.class, () -> servicio.crearNotificacion(null));
        verifyNoInteractions(repositorio);
    }

    @Test
    void elListenerDeReservaSolicitadaExigeElEvento() {
        ReservaSolicitadaNotificationListener listener = new ReservaSolicitadaNotificationListener(repositorio);

        assertThrows(DatosInvalidosException.class, () -> listener.alSolicitarReserva(null));
        verifyNoInteractions(repositorio);
    }
}
