package co.edu.unbosque.marketplace.publicacion.api.response;

import java.util.List;

public record PaginaPublicacionResponse(
        List<PublicacionResumenResponse> publicaciones,
        int pagina,
        int tamano,
        long totalResultados,
        int totalPaginas,
        String mensaje) {

    public PaginaPublicacionResponse {
        publicaciones = List.copyOf(publicaciones);
    }
}