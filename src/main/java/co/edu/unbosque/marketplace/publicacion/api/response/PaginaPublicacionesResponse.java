package co.edu.unbosque.marketplace.publicacion.api.response;

import java.util.List;

public record PaginaPublicacionesResponse(
        List<PublicacionResumenResponse> publicaciones,
        int pagina,
        int tamano,
        long totalResultados,
        int totalPaginas,
        String mensaje) {

    public PaginaPublicacionesResponse {
        publicaciones = List.copyOf(publicaciones);
    }
}