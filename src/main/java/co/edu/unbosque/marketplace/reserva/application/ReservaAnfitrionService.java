package co.edu.unbosque.marketplace.reserva.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.reserva.api.response.ReservaAlojamientoResponse;
import co.edu.unbosque.marketplace.reserva.api.response.ReservasAlojamientoResponse;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaAnfitrionDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaAnfitrionRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;

@Service
public class ReservaAnfitrionService {

    private final ReservaAnfitrionRepository historial;
    private final ReservaRepository reservas;

    public ReservaAnfitrionService(
            ReservaAnfitrionRepository historial,
            ReservaRepository reservas) {
        this.historial = historial;
        this.reservas = reservas;
    }

    @Transactional(readOnly = true)
    public ReservasAlojamientoResponse consultarReservas(UUID idAnfitrion) {
        validarAnfitrion(idAnfitrion);

        List<ReservaAlojamientoResponse> resultado = historial.consultarPorAnfitrion(idAnfitrion)
                .stream()
                .map(this::convertir)
                .toList();

        String mensaje = resultado.isEmpty()
                ? "No hay reservas asociadas a tus alojamientos."
                : null;

        return new ReservasAlojamientoResponse(resultado, resultado.size(), mensaje);
    }

    @Transactional(readOnly = true)
    public ReservaAlojamientoResponse consultarReserva(UUID idAnfitrion, UUID idReserva) {
        validarAnfitrion(idAnfitrion);
        if (idReserva == null) {
            throw new DatosInvalidosException("El identificador de la reserva es obligatorio.");
        }

        return historial.consultarReservaPorAnfitrion(idAnfitrion, idReserva)
                .map(this::convertir)
                .orElseThrow(() -> reservas.existsById(idReserva)
                        ? new AccesoNoPermitidoException(
                                "No tienes permiso para consultar esta reserva.")
                        : new RecursoNoEncontradoException("La reserva no existe."));
    }

    private void validarAnfitrion(UUID idAnfitrion) {
        if (idAnfitrion == null) {
            throw new SesionRequeridaException("Debes iniciar sesión como anfitrión.");
        }
    }

    private ReservaAlojamientoResponse convertir(ReservaAnfitrionDatos datos) {
        return new ReservaAlojamientoResponse(
                datos.idReserva(),
                datos.idAlojamiento(),
                datos.alojamiento(),
                datos.idHuesped(),
                datos.huesped(),
                datos.fechaEntrada(),
                datos.fechaSalida(),
                datos.estado(),
                datos.montoTotal(),
                datos.retencionHasta());
    }
}
