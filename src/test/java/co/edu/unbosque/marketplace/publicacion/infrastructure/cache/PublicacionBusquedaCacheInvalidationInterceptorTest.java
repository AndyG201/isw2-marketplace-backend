package co.edu.unbosque.marketplace.publicacion.infrastructure.cache;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import co.edu.unbosque.marketplace.publicacion.application.port.CacheBusquedaPublicaciones;

/** La caché de búsqueda se invalida solo cuando una escritura exitosa puede cambiar el catálogo. */
class PublicacionBusquedaCacheInvalidationInterceptorTest {

    private final CacheBusquedaPublicaciones cache = mock(CacheBusquedaPublicaciones.class);
    private final PublicacionBusquedaCacheInvalidationInterceptor interceptor =
            new PublicacionBusquedaCacheInvalidationInterceptor(cache);

    private void terminar(String metodo, String ruta, int estado, Exception error) {
        MockHttpServletRequest peticion = new MockHttpServletRequest(metodo, ruta);
        peticion.setServletPath(ruta);
        MockHttpServletResponse respuesta = new MockHttpServletResponse();
        respuesta.setStatus(estado);
        interceptor.afterCompletion(peticion, respuesta, new Object(), error);
    }

    @Test
    void unaEscrituraExitosaEnElCatalogoInvalidaLaCache() {
        terminar("POST", "/publicaciones", 201, null);
        terminar("PATCH", "/publicaciones/123/estado", 200, null);
        terminar("PUT", "/alojamientos/9", 200, null);
        terminar("DELETE", "/administracion/publicaciones/1", 204, null);
        terminar("POST", "/reservas/1/pago-simulado", 200, null);
        terminar("POST", "/cancelaciones/1", 200, null);
        terminar("PATCH", "/administracion/usuarios/1/restringir", 200, null);
        terminar("PUT", "/cuenta/perfil", 200, null);

        verify(cache, org.mockito.Mockito.times(8)).invalidar();
    }

    @Test
    void lasLecturasNoInvalidan() {
        terminar("GET", "/publicaciones/busqueda", 200, null);

        verify(cache, never()).invalidar();
    }

    @Test
    void unaEscrituraFallidaNoInvalida() {
        terminar("POST", "/publicaciones", 400, null);
        terminar("POST", "/publicaciones", 500, null);
        terminar("POST", "/publicaciones", 200, new IllegalStateException("falló"));

        verify(cache, never()).invalidar();
    }

    @Test
    void unaRutaAjenaAlCatalogoNoInvalida() {
        terminar("POST", "/autenticacion/login", 200, null);
        terminar("POST", "/verificacion-correo/confirmar", 200, null);

        verify(cache, never()).invalidar();
    }
}
