package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaReservaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoReserva;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.reserva.api.response.DecisionReservaResponse;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaDecisionEvent;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.NocheReservaJdbcRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRetencionRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaSolicitudDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaSolicitudRepository;

@Service
public class ReservaDecisionService {

    private static final String MODO_APROBACION = "APROBACION";
    private static final String PENDIENTE_APROBACION = "PENDIENTE_APROBACION";
    private static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private static final String RECHAZADA = "RECHAZADA";

    private final ReservaRetencionRepository retenciones;
    private final ReservaSolicitudRepository solicitudes;
    private final ReservaRepository reservas;
    private final NocheReservaJdbcRepository noches;
    private final AuditoriaReservaCommand auditoria;
    private final ApplicationEventPublisher eventos;

    @Value("$" + "{app.reserva.retencion.pago-minutos:20}")
    private long minutosRetencionPago;

    public ReservaDecisionService(
            ReservaRetencionRepository retenciones,
            ReservaSolicitudRepository solicitudes,
            ReservaRepository reservas,
            NocheReservaJdbcRepository noches,
            AuditoriaReservaCommand auditoria,
            ApplicationEventPublisher eventos) {
        this.retenciones = retenciones;
        this.solicitudes = solicitudes;
        this.reservas = reservas;
        this.noches = noches;
        this.auditoria = auditoria;
        this.eventos = eventos;
    }

    @Transactional
    public DecisionReservaResponse aprobar(UUID idAnfitrion, UUID idReserva) {
        return decidir(idAnfitrion, idReserva, true);
    }

    @Transactional
    public DecisionReservaResponse rechazar(UUID idAnfitrion, UUID idReserva) {
        return decidir(idAnfitrion, idReserva, false);
    }

    private DecisionReservaResponse decidir(UUID idAnfitrion, UUID idReserva, boolean aprobar) {
        if (idAnfitrion == null) {
            throw new SesionRequeridaException("Debes iniciar sesión como anfitrión.");
        }

        Reserva reserva = retenciones.bloquearPorId(idReserva)
                .orElseThrow(() -> new RecursoNoEncontradoException("La solicitud de reserva no existe."));

        ReservaSolicitudDatos datos = solicitudes.consultarDatosCotizacion(reserva.getIdCotizacion())
                .orElseThrow(() -> new ConflictoNegocioException(
                        "No fue posible consultar el alojamiento de la solicitud."));

        if (!idAnfitrion.equals(datos.getIdAnfitrion())) {
            throw new AccesoNoPermitidoException(
                    "La solicitud no corresponde a un alojamiento tuyo.");
        }

        if (!MODO_APROBACION.equals(reserva.getModoAceptacion())
                || !PENDIENTE_APROBACION.equals(reserva.getEstado())) {
            throw new ConflictoNegocioException(
                    "La solicitud ya fue resuelta o no admite una decisión. Estado actual: "
                            + reserva.getEstado() + ".");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime limiteAprobacion = reserva.getRetencionHasta();

        if (limiteAprobacion == null) {
            throw new ConflictoNegocioException(
                    "La solicitud no tiene un plazo de retención válido.");
        }

        if (!limiteAprobacion.isAfter(ahora)) {
            rechazarPorVencimiento(reserva, datos, limiteAprobacion, ahora);
            return new DecisionReservaResponse(
                    reserva.getIdReserva(),
                    reserva.getEstado(),
                    ahora,
                    reserva.getRetencionHasta(),
                    "El plazo venció. La solicitud fue rechazada y el periodo liberado.");
        }

        if (aprobar) {
            return aprobarReserva(reserva, datos, idAnfitrion, limiteAprobacion, ahora);
        }

        return rechazarReserva(reserva, datos, idAnfitrion, limiteAprobacion, ahora);
    }

    private DecisionReservaResponse aprobarReserva(
            Reserva reserva,
            ReservaSolicitudDatos datos,
            UUID idAnfitrion,
            OffsetDateTime limiteAprobacion,
            OffsetDateTime ahora) {

        if (minutosRetencionPago <= 0) {
            throw new FallaInternaException(
                    "El plazo de retención para el pago debe ser mayor que cero.");
        }

        OffsetDateTime limitePago = ahora.plusMinutes(minutosRetencionPago);
        reserva.setEstado(PENDIENTE_PAGO);
        reserva.setRetencionHasta(limitePago);
        reservas.saveAndFlush(reserva);

        auditoria.registrarEvento(new RegistrarEventoReserva(
                idAnfitrion,
                reserva.getIdReserva(),
                ahora,
                "APROBAR_SOLICITUD",
                "APROBADA_PENDIENTE_PAGO",
                "{\"retencionPagoHasta\":\"" + limitePago + "\"}"));

        eventos.publishEvent(new ReservaDecisionEvent(
                reserva.getIdHuesped(),
                reserva.getIdReserva(),
                datos.getIdPublicacion(),
                "APROBADA_PENDIENTE_PAGO",
                ahora,
                limitePago,
                limiteAprobacion));

        return new DecisionReservaResponse(
                reserva.getIdReserva(),
                reserva.getEstado(),
                ahora,
                limitePago,
                "Solicitud aprobada. El huésped ya puede pagar dentro del plazo indicado.");
    }

    private DecisionReservaResponse rechazarReserva(
            Reserva reserva,
            ReservaSolicitudDatos datos,
            UUID idAnfitrion,
            OffsetDateTime limiteAprobacion,
            OffsetDateTime ahora) {

        noches.liberar(reserva.getIdReserva());
        reserva.setEstado(RECHAZADA);
        reserva.setRetencionHasta(null);
        reservas.saveAndFlush(reserva);

        auditoria.registrarEvento(new RegistrarEventoReserva(
                idAnfitrion,
                reserva.getIdReserva(),
                ahora,
                "RECHAZAR_SOLICITUD",
                RECHAZADA,
                "{\"motivo\":\"RECHAZO_DEL_ANFITRION\"}"));

        eventos.publishEvent(new ReservaDecisionEvent(
                reserva.getIdHuesped(),
                reserva.getIdReserva(),
                datos.getIdPublicacion(),
                RECHAZADA,
                ahora,
                null,
                limiteAprobacion));

        return new DecisionReservaResponse(
                reserva.getIdReserva(),
                reserva.getEstado(),
                ahora,
                null,
                "Solicitud rechazada. El periodo quedó liberado.");
    }

    private void rechazarPorVencimiento(
            Reserva reserva,
            ReservaSolicitudDatos datos,
            OffsetDateTime limiteAprobacion,
            OffsetDateTime ahora) {

        noches.liberar(reserva.getIdReserva());
        reserva.setEstado(RECHAZADA);
        reserva.setRetencionHasta(null);
        reservas.saveAndFlush(reserva);

        auditoria.registrarEvento(new RegistrarEventoReserva(
                null,
                reserva.getIdReserva(),
                ahora,
                "RECHAZO_AUTOMATICO_POR_VENCIMIENTO",
                RECHAZADA,
                "{\"fechaVencimiento\":\"" + limiteAprobacion + "\"}"));

        eventos.publishEvent(new ReservaDecisionEvent(
                reserva.getIdHuesped(),
                reserva.getIdReserva(),
                datos.getIdPublicacion(),
                "VENCIDA",
                ahora,
                null,
                limiteAprobacion));
    }
}