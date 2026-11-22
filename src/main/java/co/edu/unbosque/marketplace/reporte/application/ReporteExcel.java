package co.edu.unbosque.marketplace.reporte.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.AxisPosition;
import org.apache.poi.xddf.usermodel.chart.BarDirection;
import org.apache.poi.xddf.usermodel.chart.ChartTypes;
import org.apache.poi.xddf.usermodel.chart.XDDFBarChartData;
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryAxis;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory;
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource;
import org.apache.poi.xddf.usermodel.chart.XDDFValueAxis;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import co.edu.unbosque.marketplace.reporte.application.dto.DashboardIndicadoresResponse;
import co.edu.unbosque.marketplace.reporte.domain.ResumenFinanciero;

/**
 * Reporte de gestión en Excel: una hoja por tema, con encabezados de color, números reales (para poder sumarlos o
 * graficarlos), dinero con formato de moneda, porcentajes y gráficas de barras.
 */
final class ReporteExcel {

    private static final byte[] INDIGO = {(byte) 0x31, (byte) 0x2e, (byte) 0x81};
    private static final byte[] VIOLETA = {(byte) 0x7c, (byte) 0x3a, (byte) 0xed};
    private static final byte[] CLARO = {(byte) 0xee, (byte) 0xf2, (byte) 0xff};
    private static final byte[] FILA_CLARA = {(byte) 0xf8, (byte) 0xfa, (byte) 0xfc};

    private final XSSFWorkbook libro = new XSSFWorkbook();
    private final CellStyle titulo = estiloTitulo();
    private final CellStyle subtitulo = estiloSubtitulo();
    private final CellStyle encabezado = estiloEncabezado(HorizontalAlignment.LEFT);
    private final CellStyle encabezadoDerecha = estiloEncabezado(HorizontalAlignment.RIGHT);
    private final CellStyle seccion = estiloSeccion();
    private final CellStyle texto = estiloCuerpo(null, false, false);
    private final CellStyle textoClaro = estiloCuerpo(null, true, false);
    private final CellStyle nota = estiloNota();
    private final CellStyle entero = estiloCuerpo("#,##0", false, false);
    private final CellStyle enteroClaro = estiloCuerpo("#,##0", true, false);
    private final CellStyle moneda = estiloCuerpo("\"$\" #,##0", false, false);
    private final CellStyle monedaClara = estiloCuerpo("\"$\" #,##0", true, false);
    private final CellStyle porcentaje = estiloCuerpo("0.0%", false, false);
    private final CellStyle porcentajeClaro = estiloCuerpo("0.0%", true, false);
    private final CellStyle totalTexto = estiloCuerpo(null, false, true);
    private final CellStyle totalEntero = estiloCuerpo("#,##0", false, true);
    private final CellStyle totalPorcentaje = estiloCuerpo("0.0%", false, true);

    private ReporteExcel() {
    }

    static void generar(Path archivo, DashboardIndicadoresResponse datos) throws IOException {
        ReporteExcel reporte = new ReporteExcel();
        try {
            reporte.resumen(datos);
            reporte.reservas(datos);
            reporte.dinero(datos);
            reporte.demanda(datos);
            try (var salida = Files.newOutputStream(archivo)) {
                reporte.libro.write(salida);
            }
        } finally {
            reporte.libro.close();
        }
    }

    // ---------------------------------------------------------------- hojas

    private void resumen(DashboardIndicadoresResponse d) {
        Sheet hoja = libro.createSheet("Resumen");
        columnas(hoja, 38, 20, 62);
        int fila = cabecera(hoja, "Reporte de gestión", "Fecha de corte: " + FormatoReporte.fechaHora(d.fechaCorteDatos()), 3);

        fila = seccion(hoja, fila, "Usuarios");
        fila = filaDato(hoja, fila, "Usuarios registrados", d.usuarios().total(), false, "Todas las cuentas creadas");
        fila = filaDato(hoja, fila, "Usuarios activos", d.usuarios().activos(), true, "Cuentas habilitadas para usar la plataforma");
        fila = filaDato(hoja, fila, "Huéspedes", d.usuarios().huespedes(), false, "");
        fila = filaDato(hoja, fila, "Anfitriones", d.usuarios().anfitriones(), true, "");

        fila = seccion(hoja, fila, "Publicaciones y reservas");
        fila = filaDato(hoja, fila, "Publicaciones activas", d.publicacionesActivas(), false, "Visibles en el marketplace");
        fila = filaDato(hoja, fila, "Reservas", FormatoReporte.total(d.reservasPorEstado()), true,
                "En todos los estados (el detalle está en la hoja Reservas por estado)");

        fila = seccion(hoja, fila, "Dinero");
        ResumenFinanciero f = d.finanzas();
        fila = filaDinero(hoja, fila, "Pagado por huéspedes", f.pagadoPorHuespedes().doubleValue(), false);
        fila = filaDinero(hoja, fila, "Devoluciones por cancelaciones", f.devoluciones().doubleValue(), true);
        fila = filaDinero(hoja, fila, "Ingresos netos", f.ingresosNetos().doubleValue(), false);
        fila = filaDinero(hoja, fila, "Comisión de la plataforma", f.comisionPlataforma().doubleValue(), true);
        fila = filaDinero(hoja, fila, "Ganancias de los anfitriones", f.gananciasAnfitriones().doubleValue(), false);

        hoja.createFreezePane(0, 2);
        impresion(hoja);
    }

    private void reservas(DashboardIndicadoresResponse d) {
        Sheet hoja = libro.createSheet("Reservas por estado");
        columnas(hoja, 30, 14, 14);
        int fila = cabecera(hoja, "Reservas por estado", "En el orden del ciclo de una reserva", 3);

        encabezado(hoja, fila++, new String[] {"Estado", "Reservas", "Del total"});
        long total = FormatoReporte.total(d.reservasPorEstado());
        int primera = fila;
        int numero = 0;
        for (String estado : FormatoReporte.estadosOrdenados(d.reservasPorEstado())) {
            long cantidad = d.reservasPorEstado().get(estado);
            boolean clara = numero++ % 2 == 1;
            Row f = hoja.createRow(fila++);
            celda(f, 0, FormatoReporte.etiquetaEstado(estado), clara ? textoClaro : texto);
            celda(f, 1, cantidad, clara ? enteroClaro : entero);
            celda(f, 2, total == 0 ? 0 : (double) cantidad / total, clara ? porcentajeClaro : porcentaje);
        }
        int ultima = fila - 1;
        Row filaTotal = hoja.createRow(fila);
        celda(filaTotal, 0, "Total", totalTexto);
        celda(filaTotal, 1, total, totalEntero);
        celda(filaTotal, 2, total == 0 ? 0 : 1.0, totalPorcentaje);

        if (ultima >= primera) {
            graficaDeBarras(hoja, "Reservas por estado", primera, ultima, 4, 2, 13, 20);
        }
        impresion(hoja);
    }

    private void dinero(DashboardIndicadoresResponse d) {
        Sheet hoja = libro.createSheet("Dinero");
        columnas(hoja, 34, 20, 70);
        int fila = cabecera(hoja, "Dinero", "Calculado sobre las reservas con pago aprobado", 3);

        encabezado(hoja, fila++, new String[] {"Concepto", "Valor", "Qué significa"});
        ResumenFinanciero f = d.finanzas();
        fila = filaConcepto(hoja, fila, "Pagado por huéspedes", f.pagadoPorHuespedes().doubleValue(),
                "Suma de los pagos aprobados", false);
        fila = filaConcepto(hoja, fila, "Devoluciones por cancelaciones", f.devoluciones().doubleValue(),
                "Lo devuelto a los huéspedes, según la política de cada publicación", true);
        fila = filaConcepto(hoja, fila, "Ingresos netos", f.ingresosNetos().doubleValue(),
                "Pagado menos devoluciones", false);
        fila = filaConcepto(hoja, fila, "Comisión de la plataforma", f.comisionPlataforma().doubleValue(),
                "Tarifa de servicio menos descuento, después de devoluciones", true);
        fila = filaConcepto(hoja, fila, "Ganancias de los anfitriones", f.gananciasAnfitriones().doubleValue(),
                "Lo que se quedan los anfitriones, después de devoluciones", false);
        Row cuenta = hoja.createRow(fila++);
        celda(cuenta, 0, "Reservas con pago aprobado", totalTexto);
        celda(cuenta, 1, f.reservasConPago(), totalEntero);
        celda(cuenta, 2, "", totalTexto);

        fila++;
        fila = seccion(hoja, fila, "Cómo se reparte el dinero");
        for (String regla : List.of(
                "La plataforma se queda con la tarifa de servicio menos el descuento (el descuento lo asume ella).",
                "El anfitrión se queda con el resto: noches, limpieza y servicios adicionales.",
                "Si la reserva se cancela, la devolución sale de ambos en la misma proporción.",
                "Se usan los porcentajes de la tarifa con la que se cotizó cada reserva, no los de hoy.")) {
            Row nuevaFila = hoja.createRow(fila++);
            celda(nuevaFila, 0, "• " + regla, nota);
            hoja.addMergedRegion(new CellRangeAddress(nuevaFila.getRowNum(), nuevaFila.getRowNum(), 0, 2));
        }
        impresion(hoja);
    }

    private void demanda(DashboardIndicadoresResponse d) {
        Sheet hoja = libro.createSheet("Demanda por ciudad");
        columnas(hoja, 30, 16, 14);
        int fila = cabecera(hoja, "Demanda por ciudad", "Según las cotizaciones solicitadas", 3);

        if (d.ciudadesConMayorDemanda().isEmpty()) {
            Row vacia = hoja.createRow(fila);
            celda(vacia, 0, "Aún no hay cotizaciones registradas.", nota);
            hoja.addMergedRegion(new CellRangeAddress(fila, fila, 0, 2));
            impresion(hoja);
            return;
        }
        encabezado(hoja, fila++, new String[] {"Ciudad", "Cotizaciones", "Del total"});
        long total = d.ciudadesConMayorDemanda().stream()
                .mapToLong(DashboardIndicadoresResponse.DemandaCiudad::solicitudesCotizacion).sum();
        int primera = fila;
        int numero = 0;
        for (DashboardIndicadoresResponse.DemandaCiudad ciudad : d.ciudadesConMayorDemanda()) {
            boolean clara = numero++ % 2 == 1;
            Row f = hoja.createRow(fila++);
            celda(f, 0, ciudad.ciudad(), clara ? textoClaro : texto);
            celda(f, 1, ciudad.solicitudesCotizacion(), clara ? enteroClaro : entero);
            celda(f, 2, total == 0 ? 0 : (double) ciudad.solicitudesCotizacion() / total,
                    clara ? porcentajeClaro : porcentaje);
        }
        graficaDeBarras(hoja, "Cotizaciones por ciudad", primera, fila - 1, 4, 2, 13, 20);
        impresion(hoja);
    }

    // ---------------------------------------------------------------- filas

    /** Título grande y subtítulo en las dos primeras filas; devuelve la siguiente fila libre. */
    private int cabecera(Sheet hoja, String textoTitulo, String textoSubtitulo, int columnas) {
        Row fila0 = hoja.createRow(0);
        fila0.setHeightInPoints(30);
        celda(fila0, 0, textoTitulo, titulo);
        for (int col = 1; col < columnas; col++) {
            celda(fila0, col, "", titulo);
        }
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, columnas - 1));
        Row fila1 = hoja.createRow(1);
        fila1.setHeightInPoints(20);
        celda(fila1, 0, textoSubtitulo, subtitulo);
        for (int col = 1; col < columnas; col++) {
            celda(fila1, col, "", subtitulo);
        }
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, columnas - 1));
        return 3;
    }

    private int seccion(Sheet hoja, int fila, String nombre) {
        Row f = hoja.createRow(fila);
        f.setHeightInPoints(20);
        celda(f, 0, nombre, seccion);
        celda(f, 1, "", seccion);
        celda(f, 2, "", seccion);
        return fila + 1;
    }

    private void encabezado(Sheet hoja, int fila, String[] titulos) {
        Row f = hoja.createRow(fila);
        f.setHeightInPoints(20);
        for (int col = 0; col < titulos.length; col++) {
            celda(f, col, titulos[col], col == 0 || titulos[col].startsWith("Qué") ? encabezado : encabezadoDerecha);
        }
    }

    private int filaDato(Sheet hoja, int fila, String concepto, long valor, boolean clara, String detalle) {
        Row f = hoja.createRow(fila);
        celda(f, 0, concepto, clara ? textoClaro : texto);
        celda(f, 1, valor, clara ? enteroClaro : entero);
        celda(f, 2, detalle, clara ? textoClaro : texto);
        return fila + 1;
    }

    private int filaDinero(Sheet hoja, int fila, String concepto, double valor, boolean clara) {
        Row f = hoja.createRow(fila);
        celda(f, 0, concepto, clara ? textoClaro : texto);
        celda(f, 1, valor, clara ? monedaClara : moneda);
        celda(f, 2, "", clara ? textoClaro : texto);
        return fila + 1;
    }

    private int filaConcepto(Sheet hoja, int fila, String concepto, double valor, String significado, boolean clara) {
        Row f = hoja.createRow(fila);
        celda(f, 0, concepto, clara ? textoClaro : texto);
        celda(f, 1, valor, clara ? monedaClara : moneda);
        celda(f, 2, significado, clara ? textoClaro : texto);
        return fila + 1;
    }

    private static void celda(Row fila, int columna, String valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    private static void celda(Row fila, int columna, double valor, CellStyle estilo) {
        Cell celda = fila.createCell(columna);
        celda.setCellValue(valor);
        celda.setCellStyle(estilo);
    }

    private static void columnas(Sheet hoja, int... caracteres) {
        for (int col = 0; col < caracteres.length; col++) {
            hoja.setColumnWidth(col, caracteres[col] * 256);
        }
    }

    private static void impresion(Sheet hoja) {
        hoja.setFitToPage(true);
        hoja.getPrintSetup().setFitWidth((short) 1);
        hoja.getPrintSetup().setFitHeight((short) 0);
        hoja.getPrintSetup().setLandscape(true);
        hoja.setDisplayGridlines(false);
    }

    // ---------------------------------------------------------------- gráficas

    /** Gráfica de columnas con las categorías de la columna A y los valores de la B entre las filas dadas. */
    private void graficaDeBarras(Sheet hoja, String nombre, int primera, int ultima, int colDesde, int filaDesde,
            int colHasta, int filaHasta) {
        XSSFDrawing dibujo = (XSSFDrawing) hoja.createDrawingPatriarch();
        XSSFClientAnchor ancla = dibujo.createAnchor(0, 0, 0, 0, colDesde, filaDesde, colHasta, filaHasta);
        XSSFChart grafica = dibujo.createChart(ancla);
        grafica.setTitleText(nombre);
        grafica.setTitleOverlay(false);

        XDDFCategoryAxis ejeCategorias = grafica.createCategoryAxis(AxisPosition.BOTTOM);
        XDDFValueAxis ejeValores = grafica.createValueAxis(AxisPosition.LEFT);
        ejeValores.setMinimum(0);

        XDDFDataSource<String> categorias =
                XDDFDataSourcesFactory.fromStringCellRange((org.apache.poi.xssf.usermodel.XSSFSheet) hoja,
                        new CellRangeAddress(primera, ultima, 0, 0));
        XDDFNumericalDataSource<Double> valores =
                XDDFDataSourcesFactory.fromNumericCellRange((org.apache.poi.xssf.usermodel.XSSFSheet) hoja,
                        new CellRangeAddress(primera, ultima, 1, 1));

        XDDFBarChartData datos = (XDDFBarChartData) grafica.createData(ChartTypes.BAR, ejeCategorias, ejeValores);
        datos.setBarDirection(BarDirection.COL);
        XDDFBarChartData.Series serie = (XDDFBarChartData.Series) datos.addSeries(categorias, valores);
        serie.setTitle(nombre, null);
        grafica.plot(datos);
    }

    // ---------------------------------------------------------------- estilos

    private CellStyle base() {
        CellStyle estilo = libro.createCellStyle();
        estilo.setVerticalAlignment(VerticalAlignment.CENTER);
        return estilo;
    }

    private Font fuente(boolean negrita, int tamano, byte[] color) {
        Font fuente = libro.createFont();
        fuente.setFontName("Calibri");
        fuente.setBold(negrita);
        fuente.setFontHeightInPoints((short) tamano);
        if (color != null) {
            ((org.apache.poi.xssf.usermodel.XSSFFont) fuente).setColor(new XSSFColor(color));
        }
        return fuente;
    }

    private void relleno(CellStyle estilo, byte[] color) {
        ((org.apache.poi.xssf.usermodel.XSSFCellStyle) estilo).setFillForegroundColor(new XSSFColor(color));
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    }

    private CellStyle estiloTitulo() {
        CellStyle estilo = base();
        estilo.setFont(fuente(true, 18, new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff}));
        relleno(estilo, INDIGO);
        estilo.setIndention((short) 1);
        return estilo;
    }

    private CellStyle estiloSubtitulo() {
        CellStyle estilo = base();
        estilo.setFont(fuente(false, 11, new byte[] {(byte) 0xe0, (byte) 0xe7, (byte) 0xff}));
        relleno(estilo, VIOLETA);
        estilo.setIndention((short) 1);
        return estilo;
    }

    private CellStyle estiloEncabezado(HorizontalAlignment alineacion) {
        CellStyle estilo = base();
        estilo.setFont(fuente(true, 11, new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff}));
        relleno(estilo, INDIGO);
        estilo.setAlignment(alineacion);
        estilo.setIndention((short) 1);
        return estilo;
    }

    private CellStyle estiloSeccion() {
        CellStyle estilo = base();
        estilo.setFont(fuente(true, 12, INDIGO));
        relleno(estilo, CLARO);
        estilo.setBorderBottom(BorderStyle.MEDIUM);
        ((org.apache.poi.xssf.usermodel.XSSFCellStyle) estilo).setBottomBorderColor(new XSSFColor(VIOLETA));
        estilo.setIndention((short) 1);
        return estilo;
    }

    private CellStyle estiloNota() {
        CellStyle estilo = base();
        estilo.setFont(fuente(false, 10, new byte[] {(byte) 0x64, (byte) 0x74, (byte) 0x8b}));
        estilo.setWrapText(true);
        estilo.setIndention((short) 1);
        return estilo;
    }

    private CellStyle estiloCuerpo(String formato, boolean clara, boolean total) {
        CellStyle estilo = base();
        estilo.setFont(fuente(total, 11, new byte[] {(byte) 0x1e, (byte) 0x1b, (byte) 0x4b}));
        if (formato != null) {
            estilo.setDataFormat(libro.createDataFormat().getFormat(formato));
            estilo.setAlignment(HorizontalAlignment.RIGHT);
        } else {
            estilo.setIndention((short) 1);
        }
        if (clara) {
            relleno(estilo, FILA_CLARA);
        }
        estilo.setBorderBottom(total ? BorderStyle.MEDIUM : BorderStyle.HAIR);
        estilo.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        if (total) {
            estilo.setBorderTop(BorderStyle.MEDIUM);
            ((org.apache.poi.xssf.usermodel.XSSFCellStyle) estilo).setTopBorderColor(new XSSFColor(INDIGO));
        }
        return estilo;
    }

}
