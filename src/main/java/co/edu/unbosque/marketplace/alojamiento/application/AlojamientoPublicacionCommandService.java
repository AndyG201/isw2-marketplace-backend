
package co.edu.unbosque.marketplace.alojamiento.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.ActualizarAlojamientoParaPublicacion;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionCommand;
import co.edu.unbosque.marketplace.alojamiento.contracts.CrearAlojamientoParaPublicacion;
import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;

@Service
public class AlojamientoPublicacionCommandService
        implements AlojamientoPublicacionCommand {

    private final AlojamientoRepository alojamientoRepository;

    public AlojamientoPublicacionCommandService(
            AlojamientoRepository alojamientoRepository) {

        this.alojamientoRepository = alojamientoRepository;
    }

    @Override
    @Transactional
    public UUID crearAlojamiento(
            CrearAlojamientoParaPublicacion solicitud) {

        validarSolicitudCreacion(solicitud);

        Alojamiento alojamiento = new Alojamiento();

        UUID idAlojamiento = UUID.randomUUID();

        alojamiento.setIdAlojamiento(idAlojamiento);
        alojamiento.setIdAnfitrion(solicitud.idAnfitrion());
        alojamiento.setUbicacion(solicitud.ubicacion().trim());
        alojamiento.setCiudad(solicitud.ciudad().trim());
        alojamiento.setTipoAlojamiento(
                solicitud.tipoAlojamiento().trim());
        alojamiento.setCapacidad(solicitud.capacidad());
        alojamiento.setFechaCreacion(OffsetDateTime.now());

        alojamientoRepository.save(alojamiento);

        return idAlojamiento;
    }

    @Override
    @Transactional
    public void actualizarAlojamiento(
            UUID idAlojamiento,
            ActualizarAlojamientoParaPublicacion solicitud) {

        if (idAlojamiento == null || solicitud == null) {
            throw new IllegalArgumentException(
                    "Los datos para actualizar el alojamiento son obligatorios.");
        }

        validarDatosAlojamiento(
                solicitud.ubicacion(),
                solicitud.ciudad(),
                solicitud.tipoAlojamiento(),
                solicitud.capacidad()
        );

        Alojamiento alojamiento = alojamientoRepository
                .findById(idAlojamiento)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado a la publicación."));

        alojamiento.setUbicacion(solicitud.ubicacion().trim());
        alojamiento.setCiudad(solicitud.ciudad().trim());
        alojamiento.setTipoAlojamiento(
                solicitud.tipoAlojamiento().trim());
        alojamiento.setCapacidad(solicitud.capacidad());

        alojamientoRepository.save(alojamiento);
    }

    private void validarSolicitudCreacion(
            CrearAlojamientoParaPublicacion solicitud) {

        if (solicitud == null || solicitud.idAnfitrion() == null) {
            throw new IllegalArgumentException(
                    "Debes proporcionar un anfitrión válido.");
        }

        validarDatosAlojamiento(
                solicitud.ubicacion(),
                solicitud.ciudad(),
                solicitud.tipoAlojamiento(),
                solicitud.capacidad()
        );
    }

    private void validarDatosAlojamiento(
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad) {

        if (ubicacion == null || ubicacion.isBlank()
                || ciudad == null || ciudad.isBlank()
                || tipoAlojamiento == null || tipoAlojamiento.isBlank()) {

            throw new IllegalArgumentException(
                    "Los datos del alojamiento son obligatorios.");
        }

        if (capacidad <= 0) {
            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero.");
        }
    }
}
