package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.PlazoVencidoException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.pago.contracts.PagoInfo;
import co.edu.unbosque.marketplace.pago.contracts.PagoSandboxGateway;
import co.edu.unbosque.marketplace.pago.contracts.RegistroPagos;
import co.edu.unbosque.marketplace.pago.contracts.ResultadoPagoSandbox;
import co.edu.unbosque.marketplace.reserva.application.dto.ConfirmacionReservaResponse;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaConfirmacionDatos;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaConfirmacionRepository;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;

@Service
public class ReservaPagoAprobadoService {

    private static final String MODO_APROBACION = "APROBACION";
    private static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private static final String CONFIRMADA = "CONFIRMADA";

    private final ReservaConfirmacionRepository reservas;
    private final RegistroPagos pagos;
    private final NocheReservaRepository noches;
    private final PagoSandboxGateway pasarela;

    public ReservaPagoAprobadoService(
            ReservaConfirmacionRepository reservas,
            RegistroPagos pagos,
            NocheReservaRepository noches,
            PagoSandboxGateway pasarela) {
        this.reservas = reservas;
        this.pagos = pagos;
        this.noches = noches;
        this.pasarela = pasarela;
    }

    @Transactional
    public ConfirmacionReservaResponse pagar(UUID idHuesped, UUID idReserva, String clave) {
        if (idHuesped == null) {
            throw new SesionRequeridaException("Debes iniciar sesión.");
        }
        if (clave == null || clave.isBlank() || clave.trim().length() > 100) {
            throw new DatosInvalidosException(
                    "Debes enviar Idempotency-Key de máximo 100 caracteres.");
        }

        String claveNormalizada = clave.trim();
        Reserva reserva = reservas.bloquearPorId(idReserva)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));

        if (!idHuesped.equals(reserva.getIdHuesped())) {
            throw new AccesoNoPermitidoException("La reserva pertenece a otro usuario.");
        }

        var pagoPorClave = pagos.buscarPorClave(claveNormalizada);
        if (pagoPorClave.isPresent()) {
            PagoInfo pago = pagoPorClave.get();
            if (!idReserva.equals(pago.idReserva())) {
                throw new ConflictoNegocioException(
                        "La clave de idempotencia ya corresponde a otra operación.");
            }
            return respuesta(reserva, pago, pago.monto(), "Resultado de pago idempotente.");
        }

        var ultimoPago = pagos.ultimoDeReserva(idReserva);
        if (ultimoPago.isPresent() && "PAGADO".equals(ultimoPago.get().estado())) {
            PagoInfo pago = ultimoPago.get();
            return respuesta(reserva, pago, pago.monto(), "La reserva ya fue pagada.");
        }

        validarReservaAprobada(reserva);

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (reserva.getRetencionHasta() == null
                || !reserva.getRetencionHasta().isAfter(ahora)) {
            throw new PlazoVencidoException("El plazo de pago venció. Solicita una nueva reserva.");
        }

        ReservaConfirmacionDatos datos = reservas.consultarDatos(reserva.getIdCotizacion())
                .orElseThrow(() -> new ConflictoNegocioException("No fue posible recuperar la cotización."));

        ResultadoPagoSandbox resultado = pasarela.procesar(datos.getMontoTotal(), claveNormalizada);
        OffsetDateTime momento = OffsetDateTime.now(ZoneOffset.UTC);

        PagoInfo pago = pagos.registrarPago(
                idReserva,
                resultado.referencia(),
                claveNormalizada,
                datos.getMontoTotal(),
                resultado.aprobado(),
                momento);

        if (resultado.aprobado()) {
            reserva.setEstado(CONFIRMADA);
            reserva.setFechaConfirmacion(momento);
            reserva.setRetencionHasta(null);
            reservas.saveAndFlush(reserva);
        } else {
            noches.liberar(idReserva);
            reserva.setRetencionHasta(null);
            reservas.saveAndFlush(reserva);
        }

        return respuesta(reserva, pago, datos.getMontoTotal(), resultado.mensaje());
    }

    private void validarReservaAprobada(Reserva reserva) {
        if (!MODO_APROBACION.equals(reserva.getModoAceptacion())
                || !PENDIENTE_PAGO.equals(reserva.getEstado())) {
            throw new ConflictoNegocioException(
                    "La reserva no está aprobada y pendiente de pago.");
        }
    }

    private ConfirmacionReservaResponse respuesta(
            Reserva reserva,
            PagoInfo pago,
            java.math.BigDecimal monto,
            String mensaje) {
        return new ConfirmacionReservaResponse(
                reserva.getIdReserva(),
                reserva.getEstado(),
                pago == null ? null : pago.estado(),
                monto,
                reserva.getRetencionHasta(),
                pago == null ? null : pago.referencia(),
                mensaje);
    }
}