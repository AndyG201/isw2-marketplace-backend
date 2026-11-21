package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaReservaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoReserva;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaRetenidaEvent;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaRetencionVencidaEvent;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaSolicitadaEvent;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.NocheReservaJdbcRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRetencionRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaSolicitudDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaSolicitudRepository;

@Service
public class ReservaRetencionService {
    private static final String MODO_APROBACION = "APROBACION";
    private static final String PENDIENTE_APROBACION = "PENDIENTE_APROBACION";
    private static final String MOTIVO_VENCIMIENTO = "VENCIMIENTO_PLAZO_24_HORAS";

    private final ReservaRepository reservas;
    private final ReservaRetencionRepository retenciones;
    private final ReservaSolicitudRepository solicitudes;
    private final NocheReservaJdbcRepository noches;
    private final AuditoriaReservaCommand auditoria;
    private final ApplicationEventPublisher eventPublisher;

    public ReservaRetencionService(ReservaRepository reservas, ReservaRetencionRepository retenciones,
            ReservaSolicitudRepository solicitudes, NocheReservaJdbcRepository noches,
            AuditoriaReservaCommand auditoria, ApplicationEventPublisher eventPublisher) {
        this.reservas = reservas;
        this.retenciones = retenciones;
        this.solicitudes = solicitudes;
        this.noches = noches;
        this.auditoria = auditoria;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Transactional
    public void retenerSolicitud(ReservaSolicitadaEvent evento) {
        if (evento == null || evento.idReserva() == null
                || !MODO_APROBACION.equals(evento.modoAceptacion())) {
            return;
        }
        Reserva reserva = reservas.findById(evento.idReserva()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva solicitada ya no existe."));
        if (!MODO_APROBACION.equals(reserva.getModoAceptacion())
                || !PENDIENTE_APROBACION.equals(reserva.getEstado())) {
            return;
        }
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        if (reserva.getRetencionHasta() != null && reserva.getRetencionHasta().isAfter(ahora)) {
            return;
        }
        ReservaSolicitudDatos datos = solicitudes.consultarDatosCotizacion(reserva.getIdCotizacion())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "No fue posible recuperar las fechas de la solicitud."));
        if (!"ACTIVA".equals(datos.getEstadoPublicacion())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La publicación dejó de estar disponible para recibir reservas.");
        }
        OffsetDateTime fechaVencimiento = reserva.getFechaSolicitud().plusHours(24);
        if (!noches.reservarRango(datos.getIdAlojamiento(), reserva.getIdReserva(),
                datos.getFechaEntrada(), datos.getFechaSalida())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "El periodo ya fue ocupado por otra reserva o no está disponible.");
        }
        reserva.setRetencionHasta(fechaVencimiento);
        reservas.saveAndFlush(reserva);
        eventPublisher.publishEvent(new ReservaRetenidaEvent(datos.getIdAnfitrion(), reserva.getIdHuesped(),
                reserva.getIdReserva(), datos.getIdPublicacion(), fechaVencimiento));
    }

    @Transactional
    public void rechazarRetencionesVencidas() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        for (UUID idReserva : retenciones.buscarRetencionesVencidas(ahora)) {
            retenciones.bloquearPorId(idReserva).ifPresent(reserva -> {
                OffsetDateTime limite = reserva.getRetencionHasta();
                if (!PENDIENTE_APROBACION.equals(reserva.getEstado())
                        || limite == null || limite.isAfter(ahora)) {
                    return;
                }
                ReservaSolicitudDatos datos = solicitudes.consultarDatosCotizacion(reserva.getIdCotizacion())
                        .orElse(null);
                noches.liberar(idReserva);
                reserva.setEstado("RECHAZADA");
                reserva.setRetencionHasta(null);
                reservas.save(reserva);
                String detalle = "{\"motivo\":\"" + MOTIVO_VENCIMIENTO
                        + "\",\"fechaVencimiento\":\"" + limite + "\"}";
                auditoria.registrarEvento(new RegistrarEventoReserva(null, idReserva, ahora,
                        "RECHAZO_AUTOMATICO_POR_VENCIMIENTO", "RECHAZADA", detalle));
                eventPublisher.publishEvent(new ReservaRetencionVencidaEvent(reserva.getIdHuesped(), idReserva,
                        datos == null ? null : datos.getIdPublicacion(), ahora, MOTIVO_VENCIMIENTO));
            });
        }
    }
}
