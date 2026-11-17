package co.edu.unbosque.marketplace.reserva.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.api.response.PagoSimuladoResponse;

@Service
public class ReservaPagoSimuladoService {

    private final ReservaPagoIntentoService intentos;
    private final PagoSandboxGateway pasarela;

    public ReservaPagoSimuladoService(ReservaPagoIntentoService intentos, PagoSandboxGateway pasarela) {
        this.intentos = intentos;
        this.pasarela = pasarela;
    }

    public PagoSimuladoResponse pagar(UUID idHuesped, UUID idReserva, String clave) {
        PagoIntentoPreparado preparado = intentos.preparar(idHuesped, idReserva, clave);
        if (!preparado.requiereProcesamiento()) {
            return respuestaPreparada(preparado);
        }
        return ejecutar(preparado);
    }

    public void conciliar(String claveIdempotencia) {
        PagoIntentoPreparado preparado = intentos.buscarPendiente(claveIdempotencia);
        if (preparado.requiereProcesamiento()) {
            ejecutar(preparado);
        }
    }

    private PagoSimuladoResponse ejecutar(PagoIntentoPreparado preparado) {
        try {
            ResultadoPagoSandbox resultado = pasarela.procesar(
                    preparado.monto(), preparado.claveIdempotencia());
            return intentos.registrarResultado(
                    preparado.claveIdempotencia(),
                    resultado.aprobado(),
                    resultado.referencia(),
                    resultado.mensaje());
        } catch (RuntimeException error) {
            return intentos.registrarError(preparado.claveIdempotencia(), error.getMessage());
        }
    }

    private PagoSimuladoResponse respuestaPreparada(PagoIntentoPreparado preparado) {
        return new PagoSimuladoResponse(
                preparado.idReserva(), preparado.idPago(), preparado.estadoReserva(), preparado.estadoPago(),
                preparado.monto(), preparado.retencionHasta(), preparado.referenciaPago(), preparado.mensaje());
    }
}
