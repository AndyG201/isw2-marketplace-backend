package co.edu.unbosque.marketplace.publicacion.application;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaInfo;
import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionReservaQuery;
import co.edu.unbosque.marketplace.publicacion.api.request.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.SolicitudCambioResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.SolicitudCambioPublicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.SolicitudCambioPublicacionRepository;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaInfo;
import co.edu.unbosque.marketplace.reserva.contracts.ReservaAfectadaQuery;

@Service
public class CambiosPublicacionService {

    static final int MAXIMO_MENSAJE = 1000;
    private static final String SEPARADOR = "\n";
    private static final Set<String> ESTADOS_APROBADOS = Set.of("ACTIVA", "PAUSADA");
    private static final Set<String> RESERVAS_VIGENTES =
            Set.of("PENDIENTE_APROBACION", "PENDIENTE_PAGO", "CONFIRMADA");

    private final SolicitudCambioPublicacionRepository solicitudes;
    private final CotizacionReservaQuery cotizaciones;
    private final ReservaAfectadaQuery reservas;

    public CambiosPublicacionService(
            SolicitudCambioPublicacionRepository solicitudes,
            CotizacionReservaQuery cotizaciones,
            ReservaAfectadaQuery reservas) {
        this.solicitudes = solicitudes;
        this.cotizaciones = cotizaciones;
        this.reservas = reservas;
    }

    public boolean requiereRevision(String estado) {
        return estado != null && ESTADOS_APROBADOS.contains(estado);
    }

    public List<String> camposSensiblesModificados(
            Publicacion publicacion,
            AlojamientoPublicacionInfo alojamiento,
            EditarPublicacionRequest solicitud) {

        List<String> campos = new ArrayList<>();
        agregarSiCambia(campos, "Título", publicacion.getTitulo(), solicitud.getTitulo());
        agregarSiCambia(campos, "Descripción", publicacion.getDescripcion(), solicitud.getDescripcion());
        agregarSiCambia(campos, "Reglas", publicacion.getReglas(), solicitud.getReglas());
        if (solicitud.getImagenes() != null && !solicitud.getImagenes().isEmpty()) {
            campos.add("Fotos");
        }
        agregarSiCambia(campos, "Ubicación", alojamiento.ubicacion(), solicitud.getUbicacion());
        agregarSiCambia(campos, "Ciudad", alojamiento.ciudad(), solicitud.getCiudad());
        agregarSiCambia(campos, "Tipo de alojamiento",
                alojamiento.tipoAlojamiento(), solicitud.getTipoAlojamiento());
        agregarSiCambia(campos, "Capacidad", alojamiento.capacidad(), solicitud.getCapacidad());
        agregarSiCambia(campos, "Habitaciones", alojamiento.habitaciones(), solicitud.getHabitaciones());
        agregarSiCambia(campos, "Camas", alojamiento.camas(), solicitud.getCamas());
        agregarSiCambia(campos, "Baños", alojamiento.banos(), solicitud.getBanos());
        if (solicitud.getPropietarioInmueble() != null) {
            agregarSiCambia(campos, "Propietario del inmueble",
                    alojamiento.propietarioInmueble(), solicitud.getPropietarioInmueble());
        }
        return campos;
    }

    public String validarMensaje(String mensaje) {
        String texto = mensaje == null ? "" : mensaje.trim();
        if (texto.isEmpty()) {
            throw new IllegalArgumentException(
                    "Explica al administrador qué cambiaste y por qué.");
        }
        if (texto.length() > MAXIMO_MENSAJE) {
            throw new IllegalArgumentException(
                    "El mensaje para el administrador no puede superar "
                            + MAXIMO_MENSAJE + " caracteres.");
        }
        return texto;
    }

    @Transactional(readOnly = true)
    public void validarCapacidad(UUID idPublicacion, int capacidadNueva, LocalDate hoy) {
        Map<UUID, CotizacionReservaInfo> futuras = cotizaciones.consultarPorPublicacion(idPublicacion)
                .stream()
                .filter(cotizacion -> cotizacion.fechaSalida() != null && cotizacion.fechaSalida().isAfter(hoy))
                .collect(Collectors.toMap(CotizacionReservaInfo::idCotizacion, Function.identity()));
        if (futuras.isEmpty()) {
            return;
        }
        int maximo = reservas.consultarPorCotizaciones(futuras.keySet()).stream()
                .filter(reserva -> RESERVAS_VIGENTES.contains(reserva.estado()))
                .map(ReservaAfectadaInfo::idCotizacion)
                .map(futuras::get)
                .filter(Objects::nonNull)
                .mapToInt(CotizacionReservaInfo::cantidadHuespedes)
                .max()
                .orElse(0);
        if (capacidadNueva < maximo) {
            throw new IllegalArgumentException(
                    "No puedes bajar la capacidad a menos de " + maximo
                            + " huéspedes porque hay reservas futuras para esa cantidad.");
        }
    }

    @Transactional
    public void registrarSolicitud(
            UUID idPublicacion,
            UUID idAnfitrion,
            String mensaje,
            List<String> campos,
            String estadoAnterior,
            OffsetDateTime fecha) {
        solicitudes.findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(idPublicacion)
                .ifPresent(anterior -> anterior.setFechaResolucion(fecha));
        solicitudes.save(new SolicitudCambioPublicacion(
                UUID.randomUUID(),
                idPublicacion,
                idAnfitrion,
                mensaje,
                String.join(SEPARADOR, campos),
                estadoAnterior,
                fecha));
    }

    @Transactional(readOnly = true)
    public Optional<SolicitudCambioResponse> pendiente(UUID idPublicacion) {
        return solicitudes.findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(idPublicacion)
                .map(solicitud -> new SolicitudCambioResponse(
                        solicitud.getMensaje(),
                        Arrays.asList(solicitud.getCampos().split(SEPARADOR)),
                        solicitud.getEstadoAnterior(),
                        solicitud.getFechaSolicitud()));
    }

    @Transactional
    public Optional<String> resolver(UUID idPublicacion, OffsetDateTime fecha) {
        return solicitudes.findFirstByIdPublicacionAndFechaResolucionIsNullOrderByFechaSolicitudDesc(idPublicacion)
                .map(solicitud -> {
                    solicitud.setFechaResolucion(fecha);
                    solicitudes.save(solicitud);
                    return solicitud.getEstadoAnterior();
                });
    }

    private static void agregarSiCambia(List<String> campos, String nombre, String actual, String nuevo) {
        if (!normalizar(actual).equals(normalizar(nuevo))) {
            campos.add(nombre);
        }
    }

    private static void agregarSiCambia(List<String> campos, String nombre, int actual, Integer nuevo) {
        if (nuevo != null && actual != nuevo) {
            campos.add(nombre);
        }
    }

    private static String normalizar(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
