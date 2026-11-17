package co.edu.unbosque.marketplace.cancelacion.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;

@Component
public class DevolucionFlexibleStrategy implements PoliticaDevolucionStrategy {

    @Override
    public boolean aplicaA(String nombrePolitica) {
        return nombrePolitica != null && "FLEXIBLE".equals(nombrePolitica.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public BigDecimal calcular(BigDecimal montoPagado, PoliticaCancelacionInfo politica, long diasAnticipacion) {
        int horasMinimas = politica.anticipacionHoras() == null ? 24 : politica.anticipacionHoras();
        long diasMinimos = (horasMinimas + 23L) / 24L;
        if (montoPagado == null || montoPagado.signum() <= 0 || diasAnticipacion < diasMinimos) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return calcularPorcentaje(montoPagado, politica.porcentajeDevolucion());
    }

    private BigDecimal calcularPorcentaje(BigDecimal monto, BigDecimal porcentaje) {
        if (porcentaje == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal porcentajeSeguro = porcentaje.max(BigDecimal.ZERO).min(new BigDecimal("100"));
        return monto.multiply(porcentajeSeguro).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
