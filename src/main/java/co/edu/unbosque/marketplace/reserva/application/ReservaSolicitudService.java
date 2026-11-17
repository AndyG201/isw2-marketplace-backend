package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.reserva.api.request.CrearReservaRequest;
import co.edu.unbosque.marketplace.reserva.api.response.SolicitudReservaResponse;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaSolicitadaEvent;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaSolicitudDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaSolicitudRepository;

@Service
public class ReservaSolicitudService {

    private static final String MODO_INMEDIATA = "INMEDIATA";
    private static final String MODO_APROBACION = "APROBACION";
    private static final String ESTADO_PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private static final String ESTADO_PENDIENTE_APROBACION =
            "PENDIENTE_APROBACION";

    private final ReservaSolicitudRepository reservaRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReservaSolicitudService(
            ReservaSolicitudRepository reservaRepository,
            ApplicationEventPublisher eventPublisher) {
        this.reservaRepository = reservaRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SolicitudReservaResponse solicitar(
            UUID idHuesped,
            CrearReservaRequest solicitud,
            String claveIdempotencia) {

        if (idHuesped == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión para solicitar una reserva.");
        }

        validarSolicitud(solicitud, claveIdempotencia);

        Optional<Reserva> repetida = reservaRepository
                .findByClaveIdempotencia(claveIdempotencia.trim());

        if (repetida.isPresent()) {
            Reserva reserva = repetida.get();
            if (!idHuesped.equals(reserva.getIdHuesped())
                    || !solicitud.idCotizacion().equals(
                            reserva.getIdCotizacion())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "La clave de idempotencia ya fue utilizada para otra solicitud.");
            }
            return convertirRespuesta(reserva);
        }

        ReservaSolicitudDatos datos = reservaRepository
                .consultarDatosCotizacion(solicitud.idCotizacion())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cotización indicada no existe."));

        if (!"ACTIVA".equals(datos.getEstadoPublicacion())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La publicación no está disponible para nuevas reservas.");
        }

        if (datos.getFechaEntrada() == null
                || datos.getFechaSalida() == null
                || !datos.getFechaSalida().isAfter(datos.getFechaEntrada())
                || datos.getFechaEntrada().isBefore(
                        OffsetDateTime.now(ZoneOffset.UTC).toLocalDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Las fechas de la cotización no son válidas.");
        }

        if (datos.getCantidadHuespedes() < 1
                || datos.getCantidadHuespedes() > datos.getCapacidad()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de huéspedes supera la capacidad del alojamiento.");
        }

        String modo = datos.getModoAceptacion();
        if (!MODO_INMEDIATA.equals(modo)
                && !MODO_APROBACION.equals(modo)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La publicación no tiene un modo de aceptación válido.");
        }

        if (!reservaRepository.rangoDisponible(
                datos.getIdAlojamiento(),
                datos.getFechaEntrada(),
                datos.getFechaSalida())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El alojamiento no está disponible para las fechas indicadas.");
        }

        if (reservaRepository.findByIdCotizacion(
                solicitud.idCotizacion()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta cotización ya tiene una solicitud de reserva.");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        boolean requiereAprobacion = MODO_APROBACION.equals(modo);

        Reserva reserva = new Reserva(
                UUID.randomUUID(),
                idHuesped,
                solicitud.idCotizacion(),
                datos.getIdPolitica(),
                modo,
                requiereAprobacion
                        ? ESTADO_PENDIENTE_APROBACION
                        : ESTADO_PENDIENTE_PAGO,
                null,
                claveIdempotencia.trim(),
                requiereAprobacion,
                ahora,
                null);

        Reserva guardada = reservaRepository.saveAndFlush(reserva);

        if (requiereAprobacion) {
            eventPublisher.publishEvent(new ReservaSolicitadaEvent(
                    datos.getIdAnfitrion(),
                    guardada.getIdReserva(),
                    datos.getIdPublicacion(),
                    modo,
                    ahora));
        }

        return convertirRespuesta(guardada);
    }

    private void validarSolicitud(
            CrearReservaRequest solicitud,
            String claveIdempotencia) {

        if (solicitud == null || solicitud.idCotizacion() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes indicar la cotización para solicitar una reserva.");
        }

        if (claveIdempotencia == null
                || claveIdempotencia.isBlank()
                || claveIdempotencia.trim().length() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes enviar una clave de idempotencia de máximo 100 caracteres.");
        }
    }

    private SolicitudReservaResponse convertirRespuesta(Reserva reserva) {
        return new SolicitudReservaResponse(
                reserva.getIdReserva(),
                reserva.getIdCotizacion(),
                reserva.getModoAceptacion(),
                reserva.getEstado(),
                reserva.getFechaSolicitud(),
                reserva.getRetencionHasta());
    }
}