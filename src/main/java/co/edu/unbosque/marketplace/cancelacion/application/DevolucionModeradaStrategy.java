package co.edu.unbosque.marketplace.cancelacion.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;

@Component
public class DevolucionModeradaStrategy implements PoliticaDevolucionStrategy {

    @Override
    public boolean aplicaA(String nombrePolitica) {
        return nombrePolitica != null && "MODERADA".equals(nombrePolitica.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public BigDecimal calcular(BigDecimal montoPagado, PoliticaCancelacionInfo politica, long diasAnticipacion) {
        int horasMinimas = politica.anticipacionHoras() == null ? 120 : politica.anticipacionHoras();
        long diasMinimos = (horasMinimas + 23L) / 24L;
        if (montoPagado == null || montoPagado.signum() <= 0 || diasAnticipacion < diasMinimos) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (politica.porcentajeDevolucion() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal porcentajeSeguro = politica.porcentajeDevolucion().max(BigDecimal.ZERO).min(new BigDecimal("100"));
        return montoPagado.multiply(porcentajeSeguro).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }
}
