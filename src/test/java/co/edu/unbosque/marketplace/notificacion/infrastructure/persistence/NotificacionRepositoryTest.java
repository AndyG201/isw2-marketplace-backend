package co.edu.unbosque.marketplace.notificacion.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.infrastructure.persistence.NotificacionRepository;

@DataJpaTest
class NotificacionRepositoryTest {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescDebeRetornarNotificacionesDelUsuario() {
        UUID idUsuario = UUID.randomUUID();
        UUID otroUsuario = UUID.randomUUID();

        Notificacion primera = crearNotificacion(
            idUsuario,
            "PRIMERA",
            OffsetDateTime.parse("2026-11-19T10:00:00-05:00")
        );

        Notificacion segunda = crearNotificacion(
            idUsuario,
            "SEGUNDA",
            OffsetDateTime.parse("2026-11-20T10:00:00-05:00")
        );

        Notificacion tercera = crearNotificacion(
            otroUsuario,
            "OTRA_NOTIFICACION",
            OffsetDateTime.parse("2026-11-21T10:00:00-05:00")
        );

        notificacionRepository.saveAll(List.of(primera, segunda, tercera));

        List<Notificacion> resultado =
            notificacionRepository.findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(
            notificacion -> idUsuario.equals(notificacion.getIdUsuario())
        ));
    }

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescDebeOrdenarPorFechaDescendente() {
        UUID idUsuario = UUID.randomUUID();

        Notificacion antigua = crearNotificacion(
            idUsuario,
            "ANTIGUA",
            OffsetDateTime.parse("2026-11-18T10:00:00-05:00")
        );

        Notificacion reciente = crearNotificacion(
            idUsuario,
            "RECIENTE",
            OffsetDateTime.parse("2026-11-21T10:00:00-05:00")
        );

        Notificacion intermedia = crearNotificacion(
            idUsuario,
            "INTERMEDIA",
            OffsetDateTime.parse("2026-11-20T10:00:00-05:00")
        );

        notificacionRepository.saveAll(
            List.of(antigua, reciente, intermedia)
        );

        List<Notificacion> resultado =
            notificacionRepository.findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);

        assertEquals(3, resultado.size());

        assertAll(
            () -> assertEquals("RECIENTE", resultado.get(0).getTipo()),
            () -> assertEquals("INTERMEDIA", resultado.get(1).getTipo()),
            () -> assertEquals("ANTIGUA", resultado.get(2).getTipo())
        );
    }

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescDebeRetornarListaVaciaCuandoNoHayResultados() {
        UUID idUsuario = UUID.randomUUID();

        List<Notificacion> resultado =
            notificacionRepository.findByIdUsuarioOrderByFechaCreacionDesc(idUsuario);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void findByIdUsuarioOrderByFechaCreacionDescNoDebeMezclarUsuarios() {
        UUID usuarioUno = UUID.randomUUID();
        UUID usuarioDos = UUID.randomUUID();

        notificacionRepository.saveAll(List.of(
            crearNotificacion(usuarioUno, "USUARIO_UNO", OffsetDateTime.now()),
            crearNotificacion(usuarioDos, "USUARIO_DOS", OffsetDateTime.now())
        ));

        List<Notificacion> resultado =
            notificacionRepository.findByIdUsuarioOrderByFechaCreacionDesc(usuarioUno);

        assertEquals(1, resultado.size());
        assertEquals(usuarioUno, resultado.get(0).getIdUsuario());
        assertEquals("USUARIO_UNO", resultado.get(0).getTipo());
    }

    private Notificacion crearNotificacion(
            UUID idUsuario,
            String tipo,
            OffsetDateTime fechaCreacion) {

        Notificacion notificacion = new Notificacion();

        notificacion.setIdNotificacion(UUID.randomUUID());
        notificacion.setIdUsuario(idUsuario);
        notificacion.setIdReserva(null);
        notificacion.setIdPublicacion(null);
        notificacion.setTipo(tipo);
        notificacion.setMensaje("Mensaje de prueba: " + tipo);
        notificacion.setFechaCreacion(fechaCreacion);
        notificacion.setFechaLectura(null);

        return notificacion;
    }
}
