package co.edu.unbosque.marketplace.reporte.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.edu.unbosque.marketplace.reporte.application.dto.DashboardIndicadoresResponse;
import co.edu.unbosque.marketplace.reporte.domain.ResumenFinanciero;

/** El reporte exportado (PDF y Excel) muestra los indicadores, con el dinero separado y con formato legible. */
class ReporteArchivoGeneratorTest {

    private static final OffsetDateTime CORTE = OffsetDateTime.parse("2026-11-22T10:00:00-05:00");

    @TempDir
    Path carpeta;

    private ReporteArchivoGenerator generadorCon(DashboardIndicadoresResponse datos) {
        DashboardIndicadoresService dashboard = mock(DashboardIndicadoresService.class);
        when(dashboard.consultar()).thenReturn(datos);
        return new ReporteArchivoGenerator(dashboard, carpeta.toString());
    }

    private DashboardIndicadoresResponse datos(List<DashboardIndicadoresResponse.DemandaCiudad> ciudades) {
        Map<String, Long> reservas = new LinkedHashMap<>();
        reservas.put("CONFIRMADA", 3L);
        reservas.put("COMPLETADA", 1L);
        reservas.put("PENDIENTE_APROBACION", 4L);
        ResumenFinanciero finanzas = new ResumenFinanciero(new BigDecimal("520000.00"), new BigDecimal("130000.00"),
                new BigDecimal("390000.00"), new BigDecimal("15000.00"), new BigDecimal("375000.00"), 2);
        return new DashboardIndicadoresResponse(new DashboardIndicadoresResponse.IndicadoresUsuarios(10, 8, 6, 2), 4,
                reservas, new BigDecimal("520000.00"), finanzas, ciudades, CORTE);
    }

    private DashboardIndicadoresResponse datos() {
        return datos(List.of(new DashboardIndicadoresResponse.DemandaCiudad("Bogotá", 5)));
    }

    private DashboardIndicadoresResponse datosVacios() {
        ResumenFinanciero ceros = new ResumenFinanciero(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, 0);
        return new DashboardIndicadoresResponse(new DashboardIndicadoresResponse.IndicadoresUsuarios(0, 0, 0, 0), 0,
                Map.of(), BigDecimal.ZERO, ceros, List.of(), CORTE);
    }

    private String textoPdf(Path ruta) throws IOException {
        try (PDDocument documento = Loader.loadPDF(ruta.toFile())) {
            return new PDFTextStripper().getText(documento);
        }
    }

    private int paginasPdf(Path ruta) throws IOException {
        try (PDDocument documento = Loader.loadPDF(ruta.toFile())) {
            return documento.getNumberOfPages();
        }
    }

    /** Todo lo que dice una hoja del Excel, para poder buscar un texto sin depender de la posición exacta. */
    private List<String> textosDeLaHoja(Sheet hoja) {
        List<String> textos = new ArrayList<>();
        for (Row fila : hoja) {
            for (Cell celda : fila) {
                if (celda.getCellType() == CellType.STRING) {
                    textos.add(celda.getStringCellValue());
                }
            }
        }
        return textos;
    }

    /** El número que está en la celda de al lado del texto dado (el valor de esa fila). */
    private double valorDeLaFila(Sheet hoja, String concepto) {
        for (Row fila : hoja) {
            Cell primera = fila.getCell(fila.getFirstCellNum());
            if (primera != null && primera.getCellType() == CellType.STRING
                    && primera.getStringCellValue().trim().equals(concepto)) {
                for (Cell celda : fila) {
                    if (celda.getCellType() == CellType.NUMERIC) {
                        return celda.getNumericCellValue();
                    }
                }
            }
        }
        throw new AssertionError("No hay una fila con valor para: " + concepto);
    }

    // ------------------------------------------------------------------ Excel

    @Test
    void elExcelTieneUnaHojaPorTema() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datos()).generar(UUID.randomUUID(), "EXCEL");

        try (InputStream entrada = Files.newInputStream(generado.ruta());
                XSSFWorkbook libro = new XSSFWorkbook(entrada)) {
            assertEquals(List.of("Resumen", "Reservas por estado", "Dinero", "Demanda por ciudad"),
                    java.util.stream.IntStream.range(0, libro.getNumberOfSheets()).mapToObj(libro::getSheetName)
                            .toList());
        }
        assertTrue(generado.ruta().toString().endsWith(".xlsx"));
        assertEquals(CORTE, generado.fechaCorte());
    }

    @Test
    void elExcelGuardaElDineroComoNumerosParaPoderSumarlo() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datos()).generar(UUID.randomUUID(), "EXCEL");

        try (InputStream entrada = Files.newInputStream(generado.ruta());
                XSSFWorkbook libro = new XSSFWorkbook(entrada)) {
            Sheet dinero = libro.getSheet("Dinero");

            assertEquals(520000d, valorDeLaFila(dinero, "Pagado por huéspedes"));
            assertEquals(130000d, valorDeLaFila(dinero, "Devoluciones por cancelaciones"));
            assertEquals(390000d, valorDeLaFila(dinero, "Ingresos netos"));
            assertEquals(15000d, valorDeLaFila(dinero, "Comisión de la plataforma"));
            assertEquals(375000d, valorDeLaFila(dinero, "Ganancias de los anfitriones"));
            assertFalse(textosDeLaHoja(dinero).contains("Ingresos estimados"));
        }
    }

    @Test
    void elExcelMuestraLosEstadosConSuNombreYSuPorcentaje() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datos()).generar(UUID.randomUUID(), "EXCEL");

        try (InputStream entrada = Files.newInputStream(generado.ruta());
                XSSFWorkbook libro = new XSSFWorkbook(entrada)) {
            Sheet reservas = libro.getSheet("Reservas por estado");
            List<String> textos = textosDeLaHoja(reservas);

            assertTrue(textos.contains("Pendiente de aprobación"), textos.toString());
            assertTrue(textos.contains("Confirmada"), textos.toString());
            assertTrue(textos.contains("Completada"), textos.toString());
            assertEquals(3d, valorDeLaFila(reservas, "Confirmada"));
            assertEquals(8d, valorDeLaFila(reservas, "Total"));
            // No quedan los códigos internos del sistema.
            assertFalse(textos.contains("PENDIENTE_APROBACION"));
        }
    }

    @Test
    void elExcelIncluyeLosGraficosDeReservasYDeCiudades() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datos()).generar(UUID.randomUUID(), "EXCEL");

        try (InputStream entrada = Files.newInputStream(generado.ruta());
                XSSFWorkbook libro = new XSSFWorkbook(entrada)) {
            XSSFSheet reservas = libro.getSheet("Reservas por estado");
            XSSFSheet ciudades = libro.getSheet("Demanda por ciudad");

            assertNotNull(reservas.getDrawingPatriarch());
            assertEquals(1, reservas.getDrawingPatriarch().getCharts().size());
            assertNotNull(ciudades.getDrawingPatriarch());
            assertEquals(1, ciudades.getDrawingPatriarch().getCharts().size());
        }
    }

    @Test
    void elExcelSinDatosNoFallaYAvisaQueNoHayCiudades() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datosVacios()).generar(UUID.randomUUID(), "EXCEL");

        try (InputStream entrada = Files.newInputStream(generado.ruta());
                XSSFWorkbook libro = new XSSFWorkbook(entrada)) {
            XSSFSheet ciudades = libro.getSheet("Demanda por ciudad");

            assertTrue(textosDeLaHoja(ciudades).stream().anyMatch(t -> t.contains("Aún no hay cotizaciones")));
            assertEquals(0d, valorDeLaFila(libro.getSheet("Dinero"), "Pagado por huéspedes"));
        }
    }

    // -------------------------------------------------------------------- PDF

    @Test
    void elPdfTraeElDineroConFormatoDePesosColombianos() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datos()).generar(UUID.randomUUID(), "PDF");
        String texto = textoPdf(generado.ruta());

        assertTrue(generado.ruta().toString().endsWith(".pdf"));
        assertTrue(texto.contains("Reporte de gestión"), texto);
        assertTrue(texto.contains("Pagado por huéspedes"), texto);
        assertTrue(texto.contains("$ 520.000"), texto);
        assertTrue(texto.contains("$ 130.000"), texto);
        assertTrue(texto.contains("$ 375.000"), texto);
        assertTrue(texto.contains("Comisión de la plataforma"), texto);
        assertFalse(texto.contains("Ingresos estimados"), texto);
        assertFalse(texto.contains("520000.00"), texto);
    }

    @Test
    void elPdfMuestraLosEstadosConSuNombreYElTotal() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datos()).generar(UUID.randomUUID(), "PDF");
        String texto = textoPdf(generado.ruta());

        assertTrue(texto.contains("Pendiente de aprobación"), texto);
        assertTrue(texto.contains("Total"), texto);
        assertTrue(texto.contains("Calculado sobre 2 reservas con pago aprobado."), texto);
        assertFalse(texto.contains("PENDIENTE_APROBACION"), texto);
    }

    @Test
    void elPdfNumeraLasPaginasYRepiteElEncabezadoSiHayMuchasCiudades() throws IOException {
        List<DashboardIndicadoresResponse.DemandaCiudad> ciudades = new ArrayList<>();
        for (int i = 1; i <= 90; i++) {
            ciudades.add(new DashboardIndicadoresResponse.DemandaCiudad("Ciudad " + i, 100 - i));
        }
        ReporteArchivoGenerado generado = generadorCon(datos(ciudades)).generar(UUID.randomUUID(), "PDF");

        int paginas = paginasPdf(generado.ruta());
        String texto = textoPdf(generado.ruta());

        assertTrue(paginas >= 3, "páginas: " + paginas);
        assertTrue(texto.contains("Página 1 de " + paginas), texto);
        assertTrue(texto.contains("Página " + paginas + " de " + paginas), texto);
        assertTrue(texto.contains("Ciudad 90"), "no se perdió ninguna fila");
    }

    @Test
    void elPdfSinDatosNoFallaYAvisaQueNoHayCotizaciones() throws IOException {
        ReporteArchivoGenerado generado = generadorCon(datosVacios()).generar(UUID.randomUUID(), "PDF");
        String texto = textoPdf(generado.ruta());

        assertEquals(1, paginasPdf(generado.ruta()));
        assertTrue(texto.contains("Aún no hay cotizaciones registradas."), texto);
        assertTrue(texto.contains("$ 0"), texto);
    }

    @Test
    void elPdfNoCortaLasCifrasMuyGrandes() throws IOException {
        ResumenFinanciero grande = new ResumenFinanciero(new BigDecimal("98765432100"), new BigDecimal("1250000000"),
                new BigDecimal("97515432100"), new BigDecimal("9876543210"), new BigDecimal("87638888890"), 40000);
        DashboardIndicadoresResponse base = datos();
        DashboardIndicadoresResponse datos = new DashboardIndicadoresResponse(base.usuarios(),
                base.publicacionesActivas(), base.reservasPorEstado(), new BigDecimal("98765432100"), grande,
                base.ciudadesConMayorDemanda(), CORTE);
        String texto = textoPdf(generadorCon(datos).generar(UUID.randomUUID(), "PDF").ruta());

        assertTrue(texto.contains("$ 98.765.432.100"), texto);
        assertTrue(texto.contains("$ 1.250.000.000"), texto);
        assertTrue(texto.contains("$ 87.638.888.890"), texto);
        assertFalse(texto.contains("..."), texto);
    }

    @Test
    void elPdfAceptaNombresConCaracteresQueLaFuenteNoTiene() throws IOException {
        DashboardIndicadoresResponse datos = datos(List.of(
                new DashboardIndicadoresResponse.DemandaCiudad("Bogotá – 東京\tZona\n2", 7)));
        ReporteArchivoGenerado generado = generadorCon(datos).generar(UUID.randomUUID(), "PDF");

        assertTrue(textoPdf(generado.ruta()).contains("Bogotá"));
    }
}
