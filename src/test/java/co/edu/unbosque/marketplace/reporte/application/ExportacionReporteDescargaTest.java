package co.edu.unbosque.marketplace.reporte.application;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaAdministracionCommand;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.PlazoVencidoException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.reporte.application.port.ExportacionReporteInfo;
import co.edu.unbosque.marketplace.reporte.application.port.ExportacionReporteRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;

class ExportacionReporteDescargaTest {

    private static final UUID ID_EXPORTACION = UUID.randomUUID();
    private static final UUID ID_ADMINISTRADOR = UUID.randomUUID();

    @TempDir
    Path carpeta;

    private ExportacionReporteRepository repositorio;
    private ExportacionReporteService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(ExportacionReporteRepository.class);
        servicio = new ExportacionReporteService(
                repositorio,
                mock(AuditoriaAdministracionCommand.class),
                mock(ColaTrabajosRedis.class),
                new ObjectMapper());
    }

    @Test
    void entregaElPdfConSuNombreYTipo() throws IOException {
        Path archivo = Files.write(carpeta.resolve("reporte.pdf"), new byte[] {1, 2, 3});
        exportacion("PDF", "COMPLETADO", archivo.toString());

        ArchivoExportado resultado = servicio.descargar(ID_EXPORTACION, ID_ADMINISTRADOR);

        assertEquals("reporte-" + ID_EXPORTACION + ".pdf", resultado.getNombre());
        assertEquals("application/pdf", resultado.getTipoContenido());
        assertArrayEquals(new byte[] {1, 2, 3}, resultado.getContenido());
    }

    @Test
    void entregaElExcelConSuNombreYTipo() throws IOException {
        Path archivo = Files.write(carpeta.resolve("reporte.xlsx"), new byte[] {4, 5});
        exportacion("XLSX", "COMPLETADO", archivo.toString());

        ArchivoExportado resultado = servicio.descargar(ID_EXPORTACION, ID_ADMINISTRADOR);

        assertEquals("reporte-" + ID_EXPORTACION + ".xlsx", resultado.getNombre());
        assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                resultado.getTipoContenido());
    }

    @Test
    void noEntregaUnaExportacionQueNoExiste() {
        when(repositorio.buscar(ID_EXPORTACION, ID_ADMINISTRADOR)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.descargar(ID_EXPORTACION, ID_ADMINISTRADOR));
    }

    @Test
    void noEntregaUnaExportacionQueSigueEnProceso() {
        exportacion("PDF", "EN_PROCESO", null);

        assertThrows(ConflictoNegocioException.class,
                () -> servicio.descargar(ID_EXPORTACION, ID_ADMINISTRADOR));
    }

    @Test
    void noEntregaUnaExportacionSinArchivo() {
        exportacion("PDF", "COMPLETADO", " ");

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.descargar(ID_EXPORTACION, ID_ADMINISTRADOR));
    }

    @Test
    void avisaCuandoElArchivoYaNoEstaEnDisco() {
        exportacion("PDF", "COMPLETADO", carpeta.resolve("borrado.pdf").toString());

        assertThrows(PlazoVencidoException.class,
                () -> servicio.descargar(ID_EXPORTACION, ID_ADMINISTRADOR));
    }

    @Test
    void elArchivoExportadoNoExponeSuContenidoInterno() {
        byte[] original = {7, 8};
        ArchivoExportado archivo = new ArchivoExportado("a.pdf", "application/pdf", original);
        original[0] = 0;
        archivo.getContenido()[1] = 0;

        assertArrayEquals(new byte[] {7, 8}, archivo.getContenido());
    }

    private void exportacion(String formato, String estado, String ruta) {
        OffsetDateTime ahora = OffsetDateTime.now();
        when(repositorio.buscar(ID_EXPORTACION, ID_ADMINISTRADOR)).thenReturn(Optional.of(
                new ExportacionReporteInfo(ID_EXPORTACION, ID_ADMINISTRADOR, formato, estado,
                        ahora, ahora, ahora, ruta)));
    }
}
