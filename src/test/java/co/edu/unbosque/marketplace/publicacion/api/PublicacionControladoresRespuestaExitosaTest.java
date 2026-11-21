package co.edu.unbosque.marketplace.publicacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import co.edu.unbosque.marketplace.publicacion.api.request.BloquearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.request.DesbloquearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.request.SeleccionarPoliticaCancelacionRequest;
import co.edu.unbosque.marketplace.publicacion.application.ModeracionPublicacionService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionBusquedaService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionCatalogoService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionConfiguracionComercialService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionModoAceptacionService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionPoliticaCancelacionService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionReservasAfectadasService;
import co.edu.unbosque.marketplace.publicacion.application.dto.ConfigurarConfiguracionComercialRequest;
import co.edu.unbosque.marketplace.publicacion.application.dto.ConfigurarModoAceptacionRequest;

/** Cubre el camino exitoso de los controladores de publicación: el servicio responde y el controlador devuelve 200. */
class PublicacionControladoresRespuestaExitosaTest {

    private final UUID usuario = UUID.randomUUID();
    private final UUID idPublicacion = UUID.randomUUID();

    private static BindingResult sinErrores(Object objetivo) {
        return new BeanPropertyBindingResult(objetivo, "solicitud");
    }

    @Test
    void busquedaDelCatalogoResponde200() {
        PublicacionBusquedaService busqueda = mock(PublicacionBusquedaService.class);
        PublicacionCatalogoController controller =
                new PublicacionCatalogoController(mock(PublicacionCatalogoService.class), busqueda);

        ResponseEntity<?> resultado = controller.buscarPublicaciones(
                "Bogotá", null, null, 2, null, null, null, null, 0, 20);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        verify(busqueda).buscar(any());
    }

    @Test
    void configuracionComercialConsultaYActualizaConRespuesta200() {
        PublicacionConfiguracionComercialService servicio = mock(PublicacionConfiguracionComercialService.class);
        PublicacionConfiguracionComercialController controller = new PublicacionConfiguracionComercialController(servicio);
        ConfigurarConfiguracionComercialRequest solicitud = new ConfigurarConfiguracionComercialRequest();

        assertEquals(HttpStatus.OK, controller.consultarConfiguracion(usuario, idPublicacion).getStatusCode());
        assertEquals(HttpStatus.OK, controller
                .actualizarConfiguracion(usuario, idPublicacion, solicitud, sinErrores(solicitud)).getStatusCode());
        verify(servicio).consultarConfiguracion(usuario, idPublicacion);
        verify(servicio).actualizarConfiguracion(usuario, idPublicacion, solicitud);
    }

    @Test
    void moderacionBloqueaYDesbloqueaConRespuesta200() {
        ModeracionPublicacionService servicio = mock(ModeracionPublicacionService.class);
        PublicacionModeracionController controller = new PublicacionModeracionController(servicio);
        BloquearPublicacionRequest bloquear = mock(BloquearPublicacionRequest.class);
        when(bloquear.getMotivo()).thenReturn("incumple normas");
        DesbloquearPublicacionRequest desbloquear = mock(DesbloquearPublicacionRequest.class);
        when(desbloquear.getMotivo()).thenReturn("ya corrigió");

        assertEquals(HttpStatus.OK, controller
                .bloquearPublicacion(usuario, idPublicacion, bloquear, sinErrores(bloquear)).getStatusCode());
        assertEquals(HttpStatus.OK, controller
                .desbloquearPublicacion(usuario, idPublicacion, desbloquear, sinErrores(desbloquear)).getStatusCode());
        verify(servicio).bloquearPublicacion(usuario, idPublicacion, "incumple normas");
        verify(servicio).desbloquearPublicacion(usuario, idPublicacion, "ya corrigió");
    }

    @Test
    void modoDeAceptacionConsultaYActualizaConRespuesta200() {
        PublicacionModoAceptacionService servicio = mock(PublicacionModoAceptacionService.class);
        PublicacionModoAceptacionController controller = new PublicacionModoAceptacionController(servicio);
        ConfigurarModoAceptacionRequest solicitud = new ConfigurarModoAceptacionRequest();
        solicitud.setModoAceptacion("INMEDIATA");

        assertEquals(HttpStatus.OK, controller.consultarModoAceptacion(usuario, idPublicacion).getStatusCode());
        assertEquals(HttpStatus.OK, controller
                .actualizarModoAceptacion(usuario, idPublicacion, solicitud, sinErrores(solicitud)).getStatusCode());
        verify(servicio).consultarModoAceptacion(usuario, idPublicacion);
        verify(servicio).actualizarModoAceptacion(usuario, idPublicacion, solicitud);
    }

    @Test
    void politicaDeCancelacionConsultaYSeleccionaConRespuesta200() {
        PublicacionPoliticaCancelacionService servicio = mock(PublicacionPoliticaCancelacionService.class);
        PublicacionPoliticaCancelacionController controller = new PublicacionPoliticaCancelacionController(servicio);
        UUID idPolitica = UUID.randomUUID();
        SeleccionarPoliticaCancelacionRequest solicitud = mock(SeleccionarPoliticaCancelacionRequest.class);
        when(solicitud.getIdPolitica()).thenReturn(idPolitica);

        assertEquals(HttpStatus.OK, controller.consultarPolitica(usuario, idPublicacion).getStatusCode());
        assertEquals(HttpStatus.OK, controller
                .seleccionarPolitica(usuario, idPublicacion, solicitud, sinErrores(solicitud)).getStatusCode());
        verify(servicio).consultarPolitica(usuario, idPublicacion);
        verify(servicio).seleccionarPolitica(usuario, idPublicacion, idPolitica);
    }

    @Test
    void reservasAfectadasResponde200() {
        PublicacionReservasAfectadasService servicio = mock(PublicacionReservasAfectadasService.class);
        PublicacionReservasAfectadasController controller = new PublicacionReservasAfectadasController(servicio);

        assertEquals(HttpStatus.OK, controller.consultarReservasAfectadas(usuario, idPublicacion).getStatusCode());
        verify(servicio).consultarReservasAfectadas(idPublicacion);
    }
}
