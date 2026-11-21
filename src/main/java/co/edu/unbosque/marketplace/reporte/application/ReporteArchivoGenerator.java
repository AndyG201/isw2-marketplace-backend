package co.edu.unbosque.marketplace.reporte.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.reporte.api.response.DashboardIndicadoresResponse;


@Component
public class ReporteArchivoGenerator {

    private final DashboardIndicadoresService dashboard;
    private final Path directorio;

    public ReporteArchivoGenerator(
            DashboardIndicadoresService dashboard,
            @Value("${app.reportes.directorio:reportes}") String directorio) {
        this.dashboard = dashboard;
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    public ReporteArchivoGenerado generar(UUID idExportacion, String formato)
            throws IOException {

        Files.createDirectories(directorio);

        DashboardIndicadoresResponse datos = dashboard.consultar();
        OffsetDateTime corte = datos.fechaCorteDatos();

        Path archivo = directorio.resolve(
                idExportacion + (formato.equals("PDF") ? ".pdf" : ".xlsx"));

        List<String[]> filas = filas(datos, corte);

        if (formato.equals("PDF")) {
            generarPdf(archivo, filas);
        } else {
            generarExcel(archivo, filas);
        }

        return new ReporteArchivoGenerado(archivo, corte);
    }

    private List<String[]> filas(DashboardIndicadoresResponse d, OffsetDateTime corte) {
        List<String[]> r = new ArrayList<>();
        r.add(new String[] {"Reporte", "Resumen de gestión"});
        r.add(new String[] {"Fecha y hora de corte", corte.toString()});
        r.add(new String[] {"Usuarios registrados", Long.toString(d.usuarios().total())});
        r.add(new String[] {"Usuarios activos", Long.toString(d.usuarios().activos())});
        r.add(new String[] {"Huéspedes", Long.toString(d.usuarios().huespedes())});
        r.add(new String[] {"Anfitriones", Long.toString(d.usuarios().anfitriones())});
        r.add(new String[] {"Publicaciones activas", Long.toString(d.publicacionesActivas())});
        r.add(new String[] {"Ingresos estimados", d.ingresosEstimados().toPlainString()});
        d.reservasPorEstado().forEach(
                (estado, cantidad) -> r.add(new String[] {"Reservas " + estado, cantidad.toString()}));
        d.ciudadesConMayorDemanda().forEach(
                c -> r.add(new String[] {"Demanda en " + c.ciudad(),
                        Long.toString(c.solicitudesCotizacion())}));
        return r;
    }

    private void generarExcel(Path archivo, List<String[]> filas) throws IOException {
        try (XSSFWorkbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Resumen de gestión");
            Row encabezado = hoja.createRow(0);
            encabezado.createCell(0).setCellValue("Indicador");
            encabezado.createCell(1).setCellValue("Valor");

            for (int i = 0; i < filas.size(); i++) {
                Row fila = hoja.createRow(i + 1);
                fila.createCell(0).setCellValue(filas.get(i)[0]);
                fila.createCell(1).setCellValue(filas.get(i)[1]);
            }
            hoja.autoSizeColumn(0);
            hoja.autoSizeColumn(1);
            try (var salida = Files.newOutputStream(archivo)) {
                libro.write(salida);
            }
        }
    }

    private void generarPdf(Path archivo, List<String[]> filas) throws IOException {
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage();
            documento.addPage(pagina);

            try (PDPageContentStream contenido =
                         new PDPageContentStream(documento, pagina)) {
                contenido.beginText();
                contenido.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                contenido.newLineAtOffset(50, 750);

                for (String[] fila : filas) {
                    contenido.showText(limpiar(fila[0] + ": " + fila[1]));
                    contenido.newLineAtOffset(0, -18);
                }
                contenido.endText();
            }
            documento.save(archivo.toFile());
        }
    }

    private String limpiar(String texto) {
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinAcentos.replaceAll("[^\\x20-\\x7E]", "?");
    }
}