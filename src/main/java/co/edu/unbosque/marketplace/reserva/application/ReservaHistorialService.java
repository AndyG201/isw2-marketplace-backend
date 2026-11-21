package co.edu.unbosque.marketplace.reserva.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.reserva.api.response.MisReservasResponse;
import co.edu.unbosque.marketplace.reserva.api.response.ReservaPropiaResponse;
import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaHistorialDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaHistorialRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;

@Service
public class ReservaHistorialService {

    private final ReservaHistorialRepository historial;
    private final ReservaRepository reservas;

    public ReservaHistorialService(
            ReservaHistorialRepository historial,
            ReservaRepository reservas) {
        this.historial = historial;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public MisReservasResponse consultarMisReservas(UUID idHuesped) {
        validarHuesped(idHuesped);

        List<ReservaPropiaResponse> resultado = historial.consultarPorHuesped(idHuesped)
                .stream()
                .map(this::convertir)
                .toList();

        String mensaje = resultado.isEmpty()
                ? "No tienes reservas para consultar."
                : null;

        return new MisReservasResponse(resultado, resultado.size(), mensaje);
    }

    @Transactional(readOnly = true)
    public ReservaPropiaResponse consultarMiReserva(UUID idHuesped, UUID idReserva) {
        validarHuesped(idHuesped);

        Reserva reserva = reservas.findById(idReserva)
                .orElseThrow(() -> new RecursoNoEncontradoException("La reserva no existe."));

        if (!idHuesped.equals(reserva.getIdHuesped())) {
            throw new AccesoNoPermitidoException(
                    "No tienes permiso para consultar esta reserva.");
        }

        ReservaHistorialDatos datos = historial.consultarPorId(idReserva)
                .orElseThrow(() -> new ConflictoNegocioException(
                        "No fue posible recuperar la información de la reserva."));

        return convertir(datos);
    }

    private void validarHuesped(UUID idHuesped) {
        if (idHuesped == null) {
            throw new SesionRequeridaException(
                    "Debes iniciar sesión como huésped.");
        }
    }

    private ReservaPropiaResponse convertir(ReservaHistorialDatos datos) {
        return new ReservaPropiaResponse(
                datos.idReserva(),
                datos.idAlojamiento(),
                datos.alojamiento(),
                datos.fechaEntrada(),
                datos.fechaSalida(),
                datos.montoTotal(),
                datos.estado(),
                datos.fechaSolicitud(),
                datos.retencionHasta(),
                datos.resenada());
    }
}