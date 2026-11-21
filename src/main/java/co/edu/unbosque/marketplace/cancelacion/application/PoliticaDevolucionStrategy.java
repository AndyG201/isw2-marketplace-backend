package co.edu.unbosque.marketplace.cancelacion.application;

import java.math.BigDecimal;

import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;

public interface PoliticaDevolucionStrategy {

    boolean aplicaA(String nombrePolitica);

    BigDecimal calcular(BigDecimal montoPagado, PoliticaCancelacionInfo politica, long diasAnticipacion);
}
