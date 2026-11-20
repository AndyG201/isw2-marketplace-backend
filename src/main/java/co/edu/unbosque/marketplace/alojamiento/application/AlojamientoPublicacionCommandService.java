
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

    private static final int MAXIMO_PROPIETARIO = 120;

    private static final int MINIMO = 1;
    private static final int MAXIMO = 50;

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
        alojamiento.setHabitaciones(valorOMinimo(solicitud.habitaciones()));
        alojamiento.setCamas(valorOMinimo(solicitud.camas()));
        alojamiento.setBanos(valorOMinimo(solicitud.banos()));
        alojamiento.setPropietarioInmueble(textoOpcional(solicitud.propietarioInmueble()));
        alojamiento.asignarPais(textoOpcional(solicitud.pais()), codigoPais(solicitud.codigoPais()));
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

        validarDistribucion(solicitud);

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

        if (solicitud.habitaciones() != null) {
            alojamiento.setHabitaciones(solicitud.habitaciones());
        }

        if (solicitud.camas() != null) {
            alojamiento.setCamas(solicitud.camas());
        }

        if (solicitud.banos() != null) {
            alojamiento.setBanos(solicitud.banos());
        }

        if (solicitud.propietarioInmueble() != null) {
            alojamiento.setPropietarioInmueble(textoOpcional(solicitud.propietarioInmueble()));
        }

        if (solicitud.pais() != null) {
            alojamiento.asignarPais(textoOpcional(solicitud.pais()), codigoPais(solicitud.codigoPais()));
        }

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

        validarDistribucion(
                solicitud.habitaciones(),
                solicitud.camas(),
                solicitud.banos()
        );
        validarPropietario(solicitud.propietarioInmueble());
    }

    private void validarPropietario(String propietario) {
        if (propietario != null && propietario.trim().length() > MAXIMO_PROPIETARIO) {
            throw new IllegalArgumentException(
                    "El nombre del propietario del inmueble no puede superar "
                            + MAXIMO_PROPIETARIO + " caracteres.");
        }
    }

    private static String codigoPais(String codigo) {
        String texto = textoOpcional(codigo);
        return texto != null && texto.matches("[A-Za-z]{2}") ? texto.toUpperCase(java.util.Locale.ROOT) : null;
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private void validarDistribucion(ActualizarAlojamientoParaPublicacion solicitud) {
        validarDistribucion(solicitud.habitaciones(), solicitud.camas(), solicitud.banos());
        validarPropietario(solicitud.propietarioInmueble());
    }

    private void validarDistribucion(
            Integer habitaciones,
            Integer camas,
            Integer banos) {

        validarCantidad(habitaciones, "habitaciones");
        validarCantidad(camas, "camas");
        validarCantidad(banos, "baños");
    }

    private void validarCantidad(Integer cantidad, String nombre) {

        if (cantidad != null && (cantidad < MINIMO || cantidad > MAXIMO)) {
            throw new IllegalArgumentException(
                    "El número de " + nombre + " debe estar entre "
                            + MINIMO + " y " + MAXIMO + ".");
        }
    }

    private static int valorOMinimo(Integer cantidad) {
        return cantidad == null ? MINIMO : cantidad;
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
