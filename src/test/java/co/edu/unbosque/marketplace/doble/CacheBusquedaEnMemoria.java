package co.edu.unbosque.marketplace.doble;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import co.edu.unbosque.marketplace.publicacion.application.dto.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.application.port.CacheBusquedaPublicaciones;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

/**
 * Versión en memoria de la caché de búsqueda, para pruebas y desarrollo sin Redis. Cumple el mismo contrato que
 * {@code PublicacionBusquedaCache} (lo comprueba {@code ContratoCacheBusquedaTest}): invalidar cambia la versión y, con
 * ella, la clave, de modo que las entradas viejas dejan de encontrarse. No aplica vigencia por tiempo.
 */
public class CacheBusquedaEnMemoria implements CacheBusquedaPublicaciones {

    private final AtomicLong version = new AtomicLong();
    private final Map<String, PaginaPublicacionesResponse> entradas = new ConcurrentHashMap<>();

    @Override
    public String versionActual() {
        return Long.toString(version.get());
    }

    @Override
    public String clave(String version, PublicacionBusquedaCriteria criteria) {
        String servicios = criteria.servicios().stream().map(Object::toString).sorted().collect(Collectors.joining(","));
        return "memoria:" + version + ":" + String.join("|",
                texto(criteria.ciudad()), texto(criteria.fechaInicio()), texto(criteria.fechaFin()),
                texto(criteria.capacidad()), texto(criteria.tipoAlojamiento()), texto(criteria.precioMinimo()),
                texto(criteria.precioMaximo()), servicios, texto(criteria.pagina()), texto(criteria.tamano()));
    }

    @Override
    public PaginaPublicacionesResponse leer(String key) {
        return entradas.get(key);
    }

    @Override
    public void guardar(String key, PaginaPublicacionesResponse resultado) {
        entradas.put(key, resultado);
    }

    @Override
    public void invalidar() {
        version.incrementAndGet();
    }

    private static String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
