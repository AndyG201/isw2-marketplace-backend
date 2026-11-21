package co.edu.unbosque.marketplace.auditoria.application.dto;

import java.util.List;

public record PaginaAuditoriaResponse(
        List<EventoAuditoriaResponse> eventos,
        int pagina,
        int tamano,
        long totalResultados,
        int totalPaginas,
        String mensaje) {
}