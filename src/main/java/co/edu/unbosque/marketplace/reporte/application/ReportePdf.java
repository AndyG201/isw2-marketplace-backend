package co.edu.unbosque.marketplace.reporte.application;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import co.edu.unbosque.marketplace.reporte.application.dto.DashboardIndicadoresResponse;
import co.edu.unbosque.marketplace.reporte.domain.ResumenFinanciero;

/**
 * Reporte de gestión en PDF: encabezado con el título y la fecha de corte, tarjetas con las cifras principales, tablas
 * por tema (con barras donde ayudan a comparar) y pie con el número de página.
 */
final class ReportePdf {

    private static final float MARGEN = 40;
    private static final float ANCHO = PDRectangle.A4.getWidth() - 2 * MARGEN;
    private static final float ALTO_FILA = 21;
    private static final float PIE = 40;

    // Colores de la aplicación (índigo y violeta) y grises para las tablas.
    private static final float[] INDIGO = {0.19f, 0.18f, 0.51f};
    private static final float[] VIOLETA = {0.49f, 0.23f, 0.93f};
    private static final float[] TEXTO = {0.12f, 0.11f, 0.29f};
    private static final float[] GRIS = {0.39f, 0.45f, 0.55f};
    private static final float[] FONDO_TARJETA = {0.93f, 0.95f, 1f};
    private static final float[] FILA_CLARA = {0.97f, 0.98f, 0.99f};
    private static final float[] LINEA = {0.89f, 0.91f, 0.94f};
    private static final float[] BLANCO = {1f, 1f, 1f};

    private final PDDocument documento = new PDDocument();
    private final PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDFont negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final String corte;

    private PDPage pagina;
    private PDPageContentStream lienzo;
    private float y;

    private ReportePdf(String corte) {
        this.corte = corte;
    }

    static void generar(Path archivo, DashboardIndicadoresResponse datos) throws IOException {
        ReportePdf reporte = new ReportePdf(FormatoReporte.fechaHora(datos.fechaCorteDatos()));
        try {
            reporte.componer(datos);
            reporte.agregarPies();
            reporte.documento.save(archivo.toFile());
        } finally {
            reporte.documento.close();
        }
    }

    // ---------------------------------------------------------------- contenido

    private void componer(DashboardIndicadoresResponse d) throws IOException {
        nuevaPagina(true);
        tarjetas(d);

        seccion("Usuarios");
        tabla(new String[] {"Concepto", "Cantidad"}, new float[] {0.7f, 0.3f}, new boolean[] {false, true},
                List.of(
                        new String[] {"Usuarios registrados", FormatoReporte.entero(d.usuarios().total())},
                        new String[] {"Usuarios activos", FormatoReporte.entero(d.usuarios().activos())},
                        new String[] {"Huéspedes", FormatoReporte.entero(d.usuarios().huespedes())},
                        new String[] {"Anfitriones", FormatoReporte.entero(d.usuarios().anfitriones())}),
                null);

        seccion("Reservas por estado");
        long totalReservas = FormatoReporte.total(d.reservasPorEstado());
        List<String> estados = FormatoReporte.estadosOrdenados(d.reservasPorEstado());
        List<String[]> filasReservas = new ArrayList<>();
        List<Double> proporciones = new ArrayList<>();
        long mayor = d.reservasPorEstado().values().stream().mapToLong(Long::longValue).max().orElse(0);
        for (String estado : estados) {
            long cantidad = d.reservasPorEstado().get(estado);
            filasReservas.add(new String[] {FormatoReporte.etiquetaEstado(estado), FormatoReporte.entero(cantidad),
                    FormatoReporte.porcentaje(cantidad, totalReservas)});
            proporciones.add(mayor == 0 ? 0 : (double) cantidad / mayor);
        }
        filasReservas.add(new String[] {"Total", FormatoReporte.entero(totalReservas), ""});
        proporciones.add(null);
        tabla(new String[] {"Estado", "Reservas", "Del total", ""}, new float[] {0.34f, 0.14f, 0.14f, 0.38f},
                new boolean[] {false, true, true, false}, filasReservas, proporciones);

        seccion("Dinero");
        ResumenFinanciero f = d.finanzas();
        tabla(new String[] {"Concepto", "Valor", "Qué significa"}, new float[] {0.28f, 0.22f, 0.50f},
                new boolean[] {false, true, false},
                List.of(
                        new String[] {"Pagado por huéspedes", FormatoReporte.moneda(f.pagadoPorHuespedes()),
                                "Suma de los pagos aprobados"},
                        new String[] {"Devoluciones", FormatoReporte.moneda(f.devoluciones()),
                                "Devuelto por cancelaciones, según la política"},
                        new String[] {"Ingresos netos", FormatoReporte.moneda(f.ingresosNetos()),
                                "Pagado menos devoluciones"},
                        new String[] {"Comisión de la plataforma", FormatoReporte.moneda(f.comisionPlataforma()),
                                "Comisión menos descuentos, tras devoluciones"},
                        new String[] {"Ganancias de los anfitriones", FormatoReporte.moneda(f.gananciasAnfitriones()),
                                "Lo que se quedan los anfitriones, tras devoluciones"}),
                null);
        nota("Calculado sobre " + FormatoReporte.entero(f.reservasConPago()) + " reservas con pago aprobado.");

        seccion("Demanda por ciudad");
        if (d.ciudadesConMayorDemanda().isEmpty()) {
            nota("Aún no hay cotizaciones registradas.");
        } else {
            long mayorDemanda = d.ciudadesConMayorDemanda().stream()
                    .mapToLong(DashboardIndicadoresResponse.DemandaCiudad::solicitudesCotizacion).max().orElse(0);
            List<String[]> filasCiudades = new ArrayList<>();
            List<Double> barras = new ArrayList<>();
            for (DashboardIndicadoresResponse.DemandaCiudad ciudad : d.ciudadesConMayorDemanda()) {
                filasCiudades.add(new String[] {ciudad.ciudad(), FormatoReporte.entero(ciudad.solicitudesCotizacion()),
                        ""});
                barras.add(mayorDemanda == 0 ? 0 : (double) ciudad.solicitudesCotizacion() / mayorDemanda);
            }
            tabla(new String[] {"Ciudad", "Cotizaciones", ""}, new float[] {0.34f, 0.18f, 0.48f},
                    new boolean[] {false, true, false}, filasCiudades, barras);
            nota("Según las cotizaciones solicitadas.");
        }
        lienzo.close();
    }

    // ---------------------------------------------------------------- piezas

    /** Cuatro tarjetas con lo más importante, para ver el estado de la plataforma de un vistazo. */
    private void tarjetas(DashboardIndicadoresResponse d) throws IOException {
        String[][] cifras = {
                {FormatoReporte.entero(d.usuarios().total()), "Usuarios registrados"},
                {FormatoReporte.entero(d.publicacionesActivas()), "Publicaciones activas"},
                {FormatoReporte.entero(FormatoReporte.total(d.reservasPorEstado())), "Reservas"},
                {FormatoReporte.moneda(d.finanzas().pagadoPorHuespedes()), "Pagado por huéspedes"}};
        float separacion = 10;
        float ancho = (ANCHO - 3 * separacion) / 4;
        float alto = 56;
        for (int i = 0; i < cifras.length; i++) {
            float x = MARGEN + i * (ancho + separacion);
            rectangulo(x, y - alto, ancho, alto, FONDO_TARJETA);
            rectangulo(x, y - alto, 3, alto, VIOLETA);
            // Una cifra muy grande se escribe más pequeña antes que cortarla.
            float tamano = 15;
            while (tamano > 8 && anchoTexto(cifras[i][0], negrita, tamano) > ancho - 18) {
                tamano -= 0.5f;
            }
            texto(ajustar(cifras[i][0], negrita, tamano, ancho - 18), x + 12, y - 25, negrita, tamano, TEXTO);
            texto(ajustar(cifras[i][1], normal, 8.5f, ancho - 18), x + 12, y - 44, normal, 8.5f, GRIS);
        }
        y -= alto + 22;
    }

    private void seccion(String titulo) throws IOException {
        asegurarEspacio(ALTO_FILA * 3);
        texto(titulo, MARGEN, y - 12, negrita, 13, INDIGO);
        linea(MARGEN, y - 19, MARGEN + ANCHO, VIOLETA, 1.2f);
        y -= 28;
    }

    private void nota(String texto) throws IOException {
        asegurarEspacio(18);
        texto(texto, MARGEN, y - 8, normal, 8.5f, GRIS);
        y -= 20;
    }

    /**
     * Tabla con encabezado de color y filas alternadas. Si {@code barras} no es nulo, la última columna dibuja una
     * barra proporcional (un valor nulo deja esa fila sin barra, por ejemplo la del total).
     */
    private void tabla(String[] encabezados, float[] proporcion, boolean[] derecha, List<String[]> filas,
            List<Double> barras) throws IOException {
        float[] anchos = new float[proporcion.length];
        for (int i = 0; i < anchos.length; i++) {
            anchos[i] = proporcion[i] * ANCHO;
        }
        filaEncabezado(encabezados, anchos, derecha);
        for (int numero = 0; numero < filas.size(); numero++) {
            if (y - ALTO_FILA < PIE + 20) {
                nuevaPagina(false);
                filaEncabezado(encabezados, anchos, derecha);
            }
            String[] fila = filas.get(numero);
            boolean esTotal = "Total".equals(fila[0]);
            if (numero % 2 == 1 && !esTotal) {
                rectangulo(MARGEN, y - ALTO_FILA, ANCHO, ALTO_FILA, FILA_CLARA);
            }
            if (esTotal) {
                linea(MARGEN, y, MARGEN + ANCHO, INDIGO, 1f);
            }
            PDFont fuente = esTotal ? negrita : normal;
            float x = MARGEN;
            for (int col = 0; col < anchos.length; col++) {
                boolean columnaBarra = barras != null && col == anchos.length - 1;
                if (columnaBarra) {
                    Double proporcionBarra = barras.get(numero);
                    if (proporcionBarra != null && proporcionBarra > 0) {
                        float largo = (float) (proporcionBarra * (anchos[col] - 16));
                        rectangulo(x + 8, y - 15, Math.max(largo, 2), 9, VIOLETA);
                    }
                } else {
                    String texto = ajustar(fila[col], fuente, 9.5f, anchos[col] - 16);
                    float ancho = anchoTexto(texto, fuente, 9.5f);
                    float posicion = derecha[col] ? x + anchos[col] - 8 - ancho : x + 8;
                    texto(texto, posicion, y - 15, fuente, 9.5f, TEXTO);
                }
                x += anchos[col];
            }
            linea(MARGEN, y - ALTO_FILA, MARGEN + ANCHO, LINEA, 0.5f);
            y -= ALTO_FILA;
        }
        y -= 10;
    }

    private void filaEncabezado(String[] encabezados, float[] anchos, boolean[] derecha) throws IOException {
        rectangulo(MARGEN, y - ALTO_FILA, ANCHO, ALTO_FILA, INDIGO);
        float x = MARGEN;
        for (int col = 0; col < anchos.length; col++) {
            String texto = ajustar(encabezados[col], negrita, 9, anchos[col] - 16);
            float ancho = anchoTexto(texto, negrita, 9);
            float posicion = derecha[col] ? x + anchos[col] - 8 - ancho : x + 8;
            texto(texto, posicion, y - 15, negrita, 9, BLANCO);
            x += anchos[col];
        }
        y -= ALTO_FILA;
    }

    // ---------------------------------------------------------------- páginas

    /** Abre una página nueva; la primera lleva el encabezado grande y las siguientes solo una línea de título. */
    private void nuevaPagina(boolean primera) throws IOException {
        if (lienzo != null) {
            lienzo.close();
        }
        pagina = new PDPage(PDRectangle.A4);
        documento.addPage(pagina);
        lienzo = new PDPageContentStream(documento, pagina);
        float alto = PDRectangle.A4.getHeight();
        if (primera) {
            rectangulo(0, alto - 110, PDRectangle.A4.getWidth(), 110, INDIGO);
            rectangulo(0, alto - 114, PDRectangle.A4.getWidth(), 4, VIOLETA);
            texto("Reporte de gestión", MARGEN, alto - 56, negrita, 24, BLANCO);
            texto("SAV  |  Marketplace de alojamientos", MARGEN, alto - 78, normal, 11, new float[] {0.88f, 0.91f, 1f});
            String fecha = "Fecha de corte: " + corte;
            texto(fecha, PDRectangle.A4.getWidth() - MARGEN - anchoTexto(fecha, normal, 9.5f), alto - 94, normal, 9.5f,
                    new float[] {0.88f, 0.91f, 1f});
            y = alto - 140;
        } else {
            texto("Reporte de gestión", MARGEN, alto - 36, negrita, 10, INDIGO);
            String fecha = "Corte: " + corte;
            texto(fecha, PDRectangle.A4.getWidth() - MARGEN - anchoTexto(fecha, normal, 8.5f), alto - 36, normal, 8.5f,
                    GRIS);
            linea(MARGEN, alto - 44, MARGEN + ANCHO, LINEA, 0.8f);
            y = alto - 64;
        }
    }

    private void asegurarEspacio(float necesario) throws IOException {
        if (y - necesario < PIE + 20) {
            nuevaPagina(false);
        }
    }

    /** Dibuja "Página X de Y" y el nombre del reporte al pie de cada página, cuando ya se sabe cuántas hay. */
    private void agregarPies() throws IOException {
        int total = documento.getNumberOfPages();
        for (int numero = 0; numero < total; numero++) {
            try (PDPageContentStream pie = new PDPageContentStream(documento, documento.getPage(numero),
                    PDPageContentStream.AppendMode.APPEND, true, true)) {
                lienzo = pie;
                linea(MARGEN, PIE, MARGEN + ANCHO, LINEA, 0.8f);
                texto("SAV  |  Reporte de gestión", MARGEN, PIE - 14, normal, 8.5f, GRIS);
                String paginas = "Página " + (numero + 1) + " de " + total;
                texto(paginas, MARGEN + ANCHO - anchoTexto(paginas, normal, 8.5f), PIE - 14, normal, 8.5f, GRIS);
            }
        }
    }

    // ---------------------------------------------------------------- dibujo

    private void rectangulo(float x, float yInferior, float ancho, float alto, float[] color) throws IOException {
        lienzo.setNonStrokingColor(color[0], color[1], color[2]);
        lienzo.addRect(x, yInferior, ancho, alto);
        lienzo.fill();
    }

    private void linea(float desde, float yLinea, float hasta, float[] color, float grosor) throws IOException {
        lienzo.setStrokingColor(color[0], color[1], color[2]);
        lienzo.setLineWidth(grosor);
        lienzo.moveTo(desde, yLinea);
        lienzo.lineTo(hasta, yLinea);
        lienzo.stroke();
    }

    private void texto(String texto, float x, float yTexto, PDFont fuente, float tamano, float[] color)
            throws IOException {
        lienzo.setNonStrokingColor(color[0], color[1], color[2]);
        lienzo.beginText();
        lienzo.setFont(fuente, tamano);
        lienzo.newLineAtOffset(x, yTexto);
        lienzo.showText(compatible(texto, fuente));
        lienzo.endText();
    }

    private float anchoTexto(String texto, PDFont fuente, float tamano) throws IOException {
        return fuente.getStringWidth(compatible(texto, fuente)) / 1000 * tamano;
    }

    /** Recorta con "..." un texto que no cabe en el ancho dado. */
    private String ajustar(String texto, PDFont fuente, float tamano, float ancho) throws IOException {
        String limpio = compatible(texto, fuente);
        if (anchoTexto(limpio, fuente, tamano) <= ancho) {
            return limpio;
        }
        String recortado = limpio;
        while (recortado.length() > 1 && anchoTexto(recortado + "...", fuente, tamano) > ancho) {
            recortado = recortado.substring(0, recortado.length() - 1);
        }
        return recortado + "...";
    }

    /**
     * La fuente estándar del PDF solo escribe caracteres del alfabeto occidental (incluye tildes y ñ). Los espacios
     * especiales pasan a espacio normal y lo que la fuente no puede escribir se cambia por "?" en vez de fallar.
     */
    private String compatible(String texto, PDFont fuente) {
        StringBuilder limpio = new StringBuilder();
        for (char caracter : texto.toCharArray()) {
            char usar = Character.isWhitespace(caracter) || Character.isSpaceChar(caracter) ? ' ' : caracter;
            try {
                fuente.encode(String.valueOf(usar));
                limpio.append(usar);
            } catch (IllegalArgumentException | IOException error) {
                limpio.append('?');
            }
        }
        return limpio.toString();
    }
}
