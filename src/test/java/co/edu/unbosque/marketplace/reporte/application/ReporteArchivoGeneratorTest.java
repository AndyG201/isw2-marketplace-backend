package co.edu.unbosque.marketplace.reporte.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.edu.unbosque.marketplace.reporte.application.dto.DashboardIndicadoresResponse;
import co.edu.unbosque.marketplace.reporte.domain.ResumenFinanciero;

/** El reporte exportado separa lo pagado, lo devuelto, la comisión de la plataforma y lo de los anfitriones. */
class ReporteArchivoGeneratorTest {

    @TempDir
    Path carpeta;

    private ReporteArchivoGenerator generador;

    @BeforeEach
    void preparar() {
        DashboardIndicadoresService dashboard = mock(DashboardIndicadoresService.class);
        Map<String, Long> reservas = new LinkedHashMap<>();
        reservas.put("CONFIRMADA", 3L);
        ResumenFinanciero finanzas = new ResumenFinanciero(new BigDecimal("520000.00"), new BigDecimal("130000.00"),
                new BigDecimal("390000.00"), new BigDecimal("15000.00"), new BigDecimal("375000.00"), 2);
        when(dashboard.consultar()).thenReturn(new DashboardIndicadoresResponse(
                new DashboardIndicadoresResponse.IndicadoresUsuarios(10, 8, 6, 2), 4, reservas,
                new BigDecimal("520000.00"), finanzas,
                List.of(new DashboardIndicadoresResponse.DemandaCiudad("Bogotá", 5)),
                OffsetDateTime.parse("2026-11-22T10:00:00-05:00")));
        generador = new ReporteArchivoGenerator(dashboard, carpeta.toString());
    }

    @Test
    void elExcelTraeLasCincoFilasDelDinero() throws IOException {
        ReporteArchivoGenerado generado = generador.generar(UUID.randomUUID(), "EXCEL");

        Map<String, String> filas = new LinkedHashMap<>();
        try (InputStream entrada = Files.newInputStream(generado.ruta());
                XSSFWorkbook libro = new XSSFWorkbook(entrada)) {
            Sheet hoja = libro.getSheetAt(0);
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                filas.put(fila.getCell(0).getStringCellValue(), fila.getCell(1).getStringCellValue());
            }
        }

        assertEquals("520000.00", filas.get("Pagado por huéspedes"));
        assertEquals("130000.00", filas.get("Devoluciones por cancelaciones"));
        assertEquals("390000.00", filas.get("Ingresos netos"));
        assertEquals("15000.00", filas.get("Comisión de la plataforma"));
        assertEquals("375000.00", filas.get("Ganancias de los anfitriones"));
        assertFalse(filas.containsKey("Ingresos estimados"));
        assertEquals("3", filas.get("Reservas CONFIRMADA"));
    }

    @Test
    void elPdfTambienTraeElDinero() throws IOException {
        ReporteArchivoGenerado generado = generador.generar(UUID.randomUUID(), "PDF");

        try (PDDocument documento = Loader.loadPDF(generado.ruta().toFile())) {
            String texto = new PDFTextStripper().getText(documento);

            assertTrue(texto.contains("Pagado por hu"), texto);
            assertTrue(texto.contains("Comisi"), texto);
            assertTrue(texto.contains("375000.00"), texto);
            assertFalse(texto.contains("Ingresos estimados"), texto);
        }
    }
}
