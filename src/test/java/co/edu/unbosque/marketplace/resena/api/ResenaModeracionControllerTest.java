package co.edu.unbosque.marketplace.resena.api;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.resena.api.request.OcultarResenaRequest;
import co.edu.unbosque.marketplace.resena.application.ResenaModeracionService;

class ResenaModeracionControllerTest {

    @Test
    void ocultarSinSesionPideIniciarSesion() {
        ResenaModeracionService servicio = mock(ResenaModeracionService.class);
        ResenaModeracionController controlador = new ResenaModeracionController(servicio);
        OcultarResenaRequest solicitud = new OcultarResenaRequest("Lenguaje ofensivo");
        UUID idResena = UUID.randomUUID();

        assertThrows(SesionRequeridaException.class,
                () -> controlador.ocultar(null, null, idResena, solicitud));
        verifyNoInteractions(servicio);
    }
}
