package co.edu.unbosque.marketplace.auditoria.application.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class PaginaAuditoriaResponseTest {

    @Test
    void conservaEventosYMetadatosDePaginacion() {
        EventoAuditoriaResponse event = new EventoAuditoriaResponse(
                null, null, null, "ACCION", "EXITOSO", "USUARIO", null, "{}", "corr-1");
        PaginaAuditoriaResponse response = new PaginaAuditoriaResponse(
                List.of(event), 2, 10, 21, 3, null);

        assertEquals(List.of(event), response.eventos());
        assertEquals(2, response.pagina());
        assertEquals(10, response.tamano());
        assertEquals(21, response.totalResultados());
        assertEquals(3, response.totalPaginas());
        assertEquals(null, response.mensaje());
    }
}