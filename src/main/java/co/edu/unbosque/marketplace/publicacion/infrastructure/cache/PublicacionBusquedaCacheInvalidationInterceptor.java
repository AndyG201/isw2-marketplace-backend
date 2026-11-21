package co.edu.unbosque.marketplace.publicacion.infrastructure.cache;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import co.edu.unbosque.marketplace.publicacion.application.port.CacheBusquedaPublicaciones;

@Component
public class PublicacionBusquedaCacheInvalidationInterceptor
        implements HandlerInterceptor {

    private final CacheBusquedaPublicaciones cache;

    public PublicacionBusquedaCacheInvalidationInterceptor(
            CacheBusquedaPublicaciones cache) {
        this.cache = cache;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        if (exception == null
                && response.getStatus() < 400
                && esEscritura(request.getMethod())
                && afectaCatalogo(request.getServletPath())) {
            cache.invalidar();
        }
    }

    private static boolean esEscritura(String method) {
        return HttpMethod.POST.matches(method)
                || HttpMethod.PUT.matches(method)
                || HttpMethod.PATCH.matches(method)
                || HttpMethod.DELETE.matches(method);
    }

    private static boolean afectaCatalogo(String path) {
        return path.equals("/publicaciones")
                || path.startsWith("/publicaciones/")
                || path.equals("/alojamientos")
                || path.startsWith("/alojamientos/")
                || path.equals("/administracion/publicaciones")
                || path.startsWith("/administracion/publicaciones/")
                || path.equals("/administracion/usuarios")
                || path.startsWith("/administracion/usuarios/")
                || path.equals("/reservas")
                || path.startsWith("/reservas/")
                || path.equals("/cancelaciones")
                || path.startsWith("/cancelaciones/")
                || path.equals("/cuenta")
                || path.startsWith("/cuenta/");
    }
}