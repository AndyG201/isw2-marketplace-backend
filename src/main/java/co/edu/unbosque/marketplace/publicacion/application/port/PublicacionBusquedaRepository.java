package co.edu.unbosque.marketplace.publicacion.application.port;

import co.edu.unbosque.marketplace.publicacion.application.dto.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

public interface PublicacionBusquedaRepository {

    PaginaPublicacionesResponse buscar(PublicacionBusquedaCriteria criteria);
}
