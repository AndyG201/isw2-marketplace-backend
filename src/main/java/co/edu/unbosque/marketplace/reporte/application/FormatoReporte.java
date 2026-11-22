package co.edu.unbosque.marketplace.reporte.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Textos y formatos que comparten el PDF y el Excel del reporte: fechas y dinero como se leen en Colombia, y los
 * nombres de los estados de la reserva como se muestran en la aplicación.
 */
final class FormatoReporte {

    static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto",
            "septiembre", "octubre", "noviembre", "diciembre"};

    /** Los estados en el orden del ciclo de una reserva, con el texto que ve el usuario. */
    private static final Map<String, String> ESTADOS_RESERVA = new LinkedHashMap<>();

    static {
        ESTADOS_RESERVA.put("PENDIENTE_APROBACION", "Pendiente de aprobación");
        ESTADOS_RESERVA.put("PENDIENTE_PAGO", "Pendiente de pago");
        ESTADOS_RESERVA.put("CONFIRMADA", "Confirmada");
        ESTADOS_RESERVA.put("COMPLETADA", "Completada");
        ESTADOS_RESERVA.put("CANCELADA", "Cancelada");
        ESTADOS_RESERVA.put("RECHAZADA", "Rechazada");
        ESTADOS_RESERVA.put("EXPIRADA", "Expirada");
    }

    private FormatoReporte() {
    }

    /** "22 de noviembre de 2026, 11:24 a. m." en hora de Colombia. */
    static String fechaHora(OffsetDateTime fecha) {
        ZonedDateTime local = fecha.atZoneSameInstant(ZONA);
        int hora12 = local.getHour() % 12 == 0 ? 12 : local.getHour() % 12;
        return local.getDayOfMonth() + " de " + MESES[local.getMonthValue() - 1] + " de " + local.getYear() + ", "
                + hora12 + ":" + String.format("%02d", local.getMinute()) + (local.getHour() < 12 ? " a. m." : " p. m.");
    }

    /** "$ 273.998" (o "-$ 10.000" si es negativo), con punto como separador de miles y sin decimales. */
    static String moneda(BigDecimal valor) {
        String numero = formatoEntero().format(valor.abs());
        return (valor.signum() < 0 ? "-$ " : "$ ") + numero;
    }

    static String entero(long valor) {
        return formatoEntero().format(valor);
    }

    /** "12,5 %" a partir de una parte y un total; "0 %" si el total es cero. */
    static String porcentaje(long parte, long total) {
        if (total <= 0) {
            return "0 %";
        }
        double valor = parte * 100.0 / total;
        DecimalFormat formato = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO")));
        return formato.format(valor) + " %";
    }

    static String etiquetaEstado(String estado) {
        return ESTADOS_RESERVA.getOrDefault(estado, capitalizar(estado.replace('_', ' ').toLowerCase(Locale.ROOT)));
    }

    /** Estados conocidos en su orden natural, y después los desconocidos, para no perder ninguno. */
    static List<String> estadosOrdenados(Map<String, Long> reservasPorEstado) {
        List<String> orden = new ArrayList<>();
        for (String estado : ESTADOS_RESERVA.keySet()) {
            if (reservasPorEstado.containsKey(estado)) {
                orden.add(estado);
            }
        }
        reservasPorEstado.keySet().stream().filter(estado -> !ESTADOS_RESERVA.containsKey(estado)).sorted()
                .forEach(orden::add);
        return orden;
    }

    static long total(Map<String, Long> reservasPorEstado) {
        return reservasPorEstado.values().stream().mapToLong(Long::longValue).sum();
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static DecimalFormat formatoEntero() {
        DecimalFormatSymbols simbolos = DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-CO"));
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        DecimalFormat formato = new DecimalFormat("#,##0", simbolos);
        formato.setRoundingMode(RoundingMode.HALF_UP);
        return formato;
    }
}
