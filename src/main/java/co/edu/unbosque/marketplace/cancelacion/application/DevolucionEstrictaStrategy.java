package co.edu.unbosque.marketplace.cancelacion.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;

@Component
public class DevolucionEstrictaStrategy implements PoliticaDevolucionStrategy {

    @Override
    public boolean aplicaA(String nombrePolitica) {
        return nombrePolitica != null && "ESTRICTA".equals(nombrePolitica.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    public BigDecimal calcular(BigDecimal montoPagado, PoliticaCancelacionInfo politica, long diasAnticipacion) {
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
}
