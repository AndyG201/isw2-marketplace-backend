package co.edu.unbosque.marketplace.pago.contracts;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroPagos {

    Optional<PagoInfo> buscarPorClave(String claveIdempotencia);

    Optional<PagoInfo> ultimoDeReserva(UUID idReserva);

    Optional<String> estadoConciliacion(String claveIdempotencia);

    boolean tieneConciliacionAbierta(UUID idReserva);

    PagoInfo iniciarIntento(UUID idReserva, String claveIdempotencia, BigDecimal monto, OffsetDateTime fecha);

    PagoInfo registrarResultado(
            String claveIdempotencia, boolean aprobado, String referencia, String mensaje, OffsetDateTime fecha);

    void registrarFallo(String claveIdempotencia, String error, OffsetDateTime fecha);

    PagoInfo registrarPago(
            UUID idReserva,
            String referencia,
            String claveIdempotencia,
            BigDecimal monto,
            boolean aprobado,
            OffsetDateTime fecha);

    List<String> clavesPorConciliar(OffsetDateTime fecha);
}
