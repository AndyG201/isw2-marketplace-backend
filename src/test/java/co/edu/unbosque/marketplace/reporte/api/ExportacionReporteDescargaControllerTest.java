package co.edu.unbosque.marketplace.reporte.api;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import co.edu.unbosque.marketplace.reporte.application.ArchivoExportado;
import co.edu.unbosque.marketplace.reporte.application.ExportacionReporteService;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionQuery;

class ExportacionReporteDescargaControllerTest {

    @Test
    void respondeElArchivoComoAdjuntoConSuTipo() {
        UUID idExportacion = UUID.randomUUID();
        UUID idAdministrador = UUID.randomUUID();
        ExportacionReporteService servicio = mock(ExportacionReporteService.class);
        UsuarioSesionQuery usuarios = mock(UsuarioSesionQuery.class);
        when(usuarios.consultarPorCorreo("admin@test.co"))
                .thenReturn(Optional.of(new UsuarioSesionInfo(idAdministrador, true)));
        when(servicio.descargar(idExportacion, idAdministrador))
                .thenReturn(new ArchivoExportado("reporte-1.pdf", "application/pdf", new byte[] {9}));
        ExportacionReporteController controlador = new ExportacionReporteController(servicio, usuarios);

        ResponseEntity<byte[]> respuesta = controlador.descargar(idExportacion,
                new UsernamePasswordAuthenticationToken("admin@test.co", null, List.of()));

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, respuesta.getHeaders().getContentType());
        assertEquals("attachment; filename=\"reporte-1.pdf\"",
                respuesta.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertArrayEquals(new byte[] {9}, respuesta.getBody());
    }
}
