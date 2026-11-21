package co.edu.unbosque.marketplace.reserva.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.reserva.application.dto.CrearReservaRequest;
import co.edu.unbosque.marketplace.reserva.application.dto.SolicitudReservaResponse;
import co.edu.unbosque.marketplace.reserva.application.port.NocheReservaRepository;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaSolicitudDatos;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaSolicitudRepository;
import co.edu.unbosque.marketplace.reserva.contracts.PeriodosHabilitadosQuery;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaSolicitadaEvent;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;

@Service
public class ReservaSolicitudService {

    private static final String MODO_INMEDIATA = "INMEDIATA";
    private static final String MODO_APROBACION = "APROBACION";
    private static final String ESTADO_PENDIENTE_PAGO = "PENDIENTE_PAGO";
    private static final String ESTADO_PENDIENTE_APROBACION =
            "PENDIENTE_APROBACION";

    private final ReservaSolicitudRepository reservaRepository;
    private final NocheReservaRepository noches;
    private final PeriodosHabilitadosQuery periodos;
    private final ApplicationEventPublisher eventPublisher;
    private final JdbcTemplate jdbc;
    private AuditoriaEventoCommand auditoria;

    public ReservaSolicitudService(
            ReservaSolicitudRepository reservaRepository,
            NocheReservaRepository noches,
            PeriodosHabilitadosQuery periodos,
            ApplicationEventPublisher eventPublisher,
            JdbcTemplate jdbc) {
        this.reservaRepository = reservaRepository;
        this.noches = noches;
        this.periodos = periodos;
        this.eventPublisher = eventPublisher;
        this.jdbc = jdbc;
    }

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
    }

    @Transactional
    public SolicitudReservaResponse solicitar(
            UUID idHuesped,
            CrearReservaRequest solicitud,
            String claveIdempotencia) {

        if (idHuesped == null) {
            throw new SesionRequeridaException(
                    "Debes iniciar sesión para solicitar una reserva.");
        }

        validarSolicitud(solicitud, claveIdempotencia);

        String idempotencia = claveIdempotencia.trim();
        bloquearClaveIdempotencia(idempotencia);

        Optional<Reserva> repetida = reservaRepository
                .findByClaveIdempotencia(idempotencia);

        if (repetida.isPresent()) {
            Reserva reserva = repetida.get();
            if (!idHuesped.equals(reserva.getIdHuesped())
                    || !solicitud.idCotizacion().equals(
                            reserva.getIdCotizacion())) {
                throw new ConflictoNegocioException(
                        "La clave de idempotencia ya fue utilizada para otra solicitud.");
            }
            return convertirRespuesta(reserva);
        }

        ReservaSolicitudDatos datos = reservaRepository
                .consultarDatosCotizacion(solicitud.idCotizacion())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La cotización indicada no existe."));

        if (!"ACTIVA".equals(datos.getEstadoPublicacion())) {
            throw new ConflictoNegocioException(
                    "La publicación no está disponible para nuevas reservas.");
        }

        if (datos.getFechaEntrada() == null
                || datos.getFechaSalida() == null
                || !datos.getFechaSalida().isAfter(datos.getFechaEntrada())
                || datos.getFechaEntrada().isBefore(
                        OffsetDateTime.now(ZoneOffset.UTC).toLocalDate())) {
            throw new DatosInvalidosException(
                    "Las fechas de la cotización no son válidas.");
        }

        if (datos.getCantidadHuespedes() < 1
                || datos.getCantidadHuespedes() > datos.getCapacidad()) {
            throw new DatosInvalidosException(
                    "La cantidad de huéspedes supera la capacidad del alojamiento.");
        }

        String modo = datos.getModoAceptacion();
        if (!MODO_INMEDIATA.equals(modo)
                && !MODO_APROBACION.equals(modo)) {
            throw new ConflictoNegocioException(
                    "La publicación no tiene un modo de aceptación válido.");
        }

        if (!periodos.rangoHabilitado(
                datos.getIdAlojamiento(),
                datos.getFechaEntrada(),
                datos.getFechaSalida())
                || noches.hayNochesOcupadas(
                        datos.getIdAlojamiento(),
                        datos.getFechaEntrada(),
                        datos.getFechaSalida())) {
            throw new ConflictoNegocioException(
                    "El alojamiento no está disponible para las fechas indicadas.");
        }

        if (reservaRepository.findByIdCotizacion(
                solicitud.idCotizacion()).isPresent()) {
            throw new ConflictoNegocioException(
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
                idempotencia,
                requiereAprobacion,
                ahora,
                null);

        Reserva guardada = reservaRepository.saveAndFlush(reserva);
        if (auditoria != null) {
            auditoria.registrarEvento(new RegistrarEventoAuditoria(
                    idHuesped, ahora, "SOLICITAR_RESERVA", "EXITOSO", "RESERVA",
                    guardada.getIdReserva(),
                    "{\"idCotizacion\":\"" + guardada.getIdCotizacion()
                            + "\",\"estado\":\"" + guardada.getEstado() + "\"}",
                    null));
        }

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

    private void bloquearClaveIdempotencia(String clave) {
        jdbc.query(
                "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
                resultado -> {
                    resultado.next();
                    return null;
                },
                "reserva:" + clave);
    }

    private void validarSolicitud(
            CrearReservaRequest solicitud,
            String claveIdempotencia) {

        if (solicitud == null || solicitud.idCotizacion() == null) {
            throw new DatosInvalidosException(
                    "Debes indicar la cotización para solicitar una reserva.");
        }

        if (claveIdempotencia == null
                || claveIdempotencia.isBlank()
                || claveIdempotencia.trim().length() > 100) {
            throw new DatosInvalidosException(
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