package co.edu.unbosque.marketplace.resena.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.resena.api.request.CrearResenaRequest;
import co.edu.unbosque.marketplace.resena.api.response.CrearResenaResponse;
import co.edu.unbosque.marketplace.resena.domain.Resena;
import co.edu.unbosque.marketplace.resena.infrastructure.persistence.ResenaRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaResenaQuery;

@Service
public class ResenaService {

    private static final String RESERVA_COMPLETADA = "COMPLETADA";

    private final ResenaRepository resenas;
    private final ReservaResenaQuery reservas;
    private AuditoriaEventoCommand auditoria;

    public ResenaService(ResenaRepository resenas, ReservaResenaQuery reservas) {
        this.resenas = resenas;
        this.reservas = reservas;
    }

    @Autowired
    public void configurarAuditoria(AuditoriaEventoCommand auditoria) {
        this.auditoria = auditoria;
    }

    @Transactional
    public CrearResenaResponse crear(UUID idHuesped, UUID idReserva, CrearResenaRequest solicitud) {
        if (idHuesped == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión como huésped.");
        }
        if (idReserva == null || solicitud == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reserva y la reseña son obligatorias.");
        }
        validarContenido(solicitud);

        ReservaResenaInfo reserva = reservas.consultarReservaParaResena(idReserva)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La reserva no existe."));
        if (!idHuesped.equals(reserva.idHuesped())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La reserva no pertenece al huésped autenticado.");
        }
        if (!RESERVA_COMPLETADA.equals(reserva.estado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Solo puedes reseñar una reserva propia que esté completada.");
        }
        if (resenas.findByIdReserva(idReserva).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta reserva ya tiene una reseña registrada.");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        Resena resena = new Resena(
                UUID.randomUUID(),
                idReserva,
                solicitud.calificacion(),
                solicitud.comentario().trim(),
                true,
                ahora,
                null,
                null,
                null);

        try {
            Resena guardada = resenas.saveAndFlush(resena);
            if (auditoria != null) {
                auditoria.registrarEvento(new RegistrarEventoAuditoria(
                        idHuesped, ahora, "CREAR_RESENA", "EXITOSO", "RESENA",
                        guardada.getIdResena(),
                        "{\"idReserva\":\"" + idReserva
                                + "\",\"calificacion\":" + guardada.getCalificacion() + "}", null));
            }
            return convertir(guardada, reserva, "La reseña y la calificación se registraron correctamente.");
        } catch (DataIntegrityViolationException error) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esta reserva ya tiene una reseña registrada.", error);
        }
    }

    private void validarContenido(CrearResenaRequest solicitud) {
        if (solicitud.calificacion() < 1 || solicitud.calificacion() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La calificación debe estar entre 1 y 5.");
        }
        if (solicitud.comentario() == null || solicitud.comentario().isBlank()
                || solicitud.comentario().trim().length() > 5000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El comentario es obligatorio y no puede superar 5000 caracteres.");
        }
    }

    private CrearResenaResponse convertir(Resena resena, ReservaResenaInfo reserva, String mensaje) {
        return new CrearResenaResponse(
                resena.getIdResena(),
                reserva.idReserva(),
                reserva.idHuesped(),
                reserva.idAlojamiento(),
                reserva.idPublicacion(),
                resena.getCalificacion(),
                resena.getComentario(),
                resena.isVisible(),
                resena.getFechaPublicacion(),
                mensaje);
    }
}
