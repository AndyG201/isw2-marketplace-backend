package co.edu.unbosque.marketplace.publicacion.application.port;

import co.edu.unbosque.marketplace.publicacion.application.dto.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

public interface CacheBusquedaPublicaciones {

    String versionActual();

    String clave(String version, PublicacionBusquedaCriteria criteria);

    PaginaPublicacionesResponse leer(String key);

    void guardar(String key, PaginaPublicacionesResponse resultado);

    void invalidar();
}
