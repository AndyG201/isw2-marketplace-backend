package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import co.edu.unbosque.marketplace.cancelacion.domain.PoliticaCancelacion;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import co.edu.unbosque.marketplace.alojamiento.api.request.CrearAlojamientoRequest;
import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.publicacion.api.request.CrearPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.storage.AlmacenamientoImagenService;
import co.edu.unbosque.marketplace.servicio.domain.Servicio;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import co.edu.unbosque.marketplace.usuario.infrastructure.persistence.UsuarioRepository;


@Service
public class PublicacionAlojamientoService {

    private final AlojamientoRepository alojamientoRepository;
    private final PublicacionRepository publicacionRepository;
    private final PublicacionServicioRepository publicacionServicioRepository;
    private final ImagenPublicacionRepository imagenPublicacionRepository;
    private final ServicioRepository servicioRepository;
    private final PoliticaCancelacionRepository politicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlmacenamientoImagenService almacenamientoImagenService;

    public PublicacionAlojamientoService(
            AlojamientoRepository alojamientoRepository,
            PublicacionRepository publicacionRepository,
            PublicacionServicioRepository publicacionServicioRepository,
            ImagenPublicacionRepository imagenPublicacionRepository,
            ServicioRepository servicioRepository,
            PoliticaCancelacionRepository politicaRepository,
            UsuarioRepository usuarioRepository,
            AlmacenamientoImagenService almacenamientoImagenService) {

        this.alojamientoRepository = alojamientoRepository;
        this.publicacionRepository = publicacionRepository;
        this.publicacionServicioRepository = publicacionServicioRepository;
        this.imagenPublicacionRepository = imagenPublicacionRepository;
        this.servicioRepository = servicioRepository;
        this.politicaRepository = politicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.almacenamientoImagenService = almacenamientoImagenService;
    }

    @Transactional
    public PublicacionAlojamientoResponse crearPublicacion(
            UUID idAnfitrion,
            CrearPublicacionRequest solicitud) {

        validarSolicitud(idAnfitrion, solicitud);

        Usuario anfitrion = usuarioRepository.findById(idAnfitrion)
                .orElseThrow(() ->
                        new IllegalArgumentException("Anfitrión no encontrado."));

        if (anfitrion.getRol() == null
                || !"ANFITRION".equalsIgnoreCase(
                        anfitrion.getRol().getNombre())) {

            throw new IllegalStateException(
                    "Solo un usuario con rol ANFITRION puede crear alojamientos.");
        }

        if (!anfitrion.isCorreoVerificado()
                || anfitrion.getEstado() != EstadoUsuario.ACTIVA
                || anfitrion.isRestringido()
                || anfitrion.getIntentosFallidos() >= 5) {

            throw new IllegalStateException(
                    "La cuenta no está habilitada para crear publicaciones.");
        }

        PoliticaCancelacion politica = politicaRepository
                .findByIdPoliticaAndActivaTrue(solicitud.getIdPolitica())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La política seleccionada no existe o está inactiva."));

        Set<UUID> idsServicios =
                new HashSet<>(solicitud.getIdsServicios());

        if (idsServicios.size() != solicitud.getIdsServicios().size()) {
            throw new IllegalArgumentException(
                    "No puedes seleccionar un mismo servicio varias veces.");
        }

        List<Servicio> servicios =
                servicioRepository.findAllById(idsServicios);

        if (servicios.size() != idsServicios.size()) {
            throw new IllegalArgumentException(
                    "Uno o varios servicios seleccionados no existen.");
        }

        List<String> rutasGuardadas = new ArrayList<>();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCompletion(int status) {

                        if (status == STATUS_ROLLED_BACK) {
                            rutasGuardadas.forEach(
                                    almacenamientoImagenService::eliminar
                            );
                        }
                    }
                }
        );

        OffsetDateTime ahora = OffsetDateTime.now();

        Alojamiento alojamiento = new Alojamiento();

        alojamiento.setIdAlojamiento(UUID.randomUUID());
        alojamiento.setIdAnfitrion(idAnfitrion);
        alojamiento.setUbicacion(solicitud.getUbicacion().trim());
        alojamiento.setCiudad(solicitud.getCiudad().trim());
        alojamiento.setTipoAlojamiento(
                solicitud.getTipoAlojamiento().trim());
        alojamiento.setCapacidad(solicitud.getCapacidad());
        alojamiento.setFechaCreacion(ahora);

        alojamientoRepository.save(alojamiento);

        Publicacion publicacion = new Publicacion();

        publicacion.setIdPublicacion(UUID.randomUUID());
        publicacion.setIdAlojamiento(alojamiento.getIdAlojamiento());
        publicacion.setIdPolitica(politica.getIdPolitica());
        publicacion.setTitulo(solicitud.getTitulo().trim());
        publicacion.setDescripcion(solicitud.getDescripcion().trim());
        publicacion.setReglas(solicitud.getReglas().trim());
        publicacion.setPrecioPorNoche(solicitud.getPrecioPorNoche());
        publicacion.setTarifaLimpieza(
                solicitud.getTarifaLimpieza() == null
                        ? java.math.BigDecimal.ZERO
                        : solicitud.getTarifaLimpieza());
        publicacion.setModoAceptacion(
                solicitud.getModoAceptacion().trim());
        publicacion.setEstado("BORRADOR");
        publicacion.setFechaCreacion(ahora);
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        for (UUID idServicio : idsServicios) {

            PublicacionServicio relacion =
                    new PublicacionServicio(
                            publicacion.getIdPublicacion(),
                            idServicio
                    );

            publicacionServicioRepository.save(relacion);
        }

        List<String> rutasImagenes = new ArrayList<>();

        for (int i = 0; i < solicitud.getImagenes().size(); i++) {

            String ruta = almacenamientoImagenService.guardar(
                    solicitud.getImagenes().get(i)
            );

            rutasGuardadas.add(ruta);
            rutasImagenes.add(ruta);

            ImagenPublicacion imagen = new ImagenPublicacion();

            imagen.setIdImagen(UUID.randomUUID());
            imagen.setIdPublicacion(publicacion.getIdPublicacion());
            imagen.setRutaImagen(ruta);
            imagen.setOrden(i + 1);
            imagen.setEsPortada(i == 0);

            imagenPublicacionRepository.save(imagen);
        }

        return new PublicacionAlojamientoResponse(
                alojamiento.getIdAlojamiento(),
                publicacion.getIdPublicacion(),
                publicacion.getTitulo(),
                publicacion.getEstado(),
                rutasImagenes
        );
    }

    private void validarSolicitud(
            UUID idAnfitrion,
            CrearPublicacionRequest solicitud) {

        if (idAnfitrion == null || solicitud == null) {
            throw new IllegalArgumentException(
                    "La solicitud de creación no es válida.");
        }

        if (solicitud.getCapacidad() == null
                || solicitud.getCapacidad() <= 0) {

            throw new IllegalArgumentException(
                    "La capacidad debe ser mayor que cero.");
        }

        if (solicitud.getPrecioPorNoche() == null
                || solicitud.getPrecioPorNoche().signum() <= 0) {

            throw new IllegalArgumentException(
                    "El precio por noche debe ser mayor que cero.");
        }

        if (solicitud.getTarifaLimpieza() != null
                && solicitud.getTarifaLimpieza().signum() < 0) {

            throw new IllegalArgumentException(
                    "La tarifa de limpieza no puede ser negativa.");
        }

        if (solicitud.getIdsServicios() == null
                || solicitud.getIdsServicios().isEmpty()
                || solicitud.getIdsServicios().size() > 20
                || solicitud.getIdsServicios().contains(null)) {

            throw new IllegalArgumentException(
                    "Debes seleccionar entre 1 y 20 servicios válidos.");
        }

        if (solicitud.getImagenes() == null
                || solicitud.getImagenes().isEmpty()
                || solicitud.getImagenes().size() > 10
                || solicitud.getImagenes().stream()
                        .anyMatch(imagen -> imagen == null || imagen.isEmpty())) {

            throw new IllegalArgumentException(
                    "Debes cargar entre 1 y 10 imágenes válidas.");
        }

        if (solicitud.getIdPolitica() == null) {
            throw new IllegalArgumentException(
                    "Debes seleccionar una política de cancelación.");
        }
    }
}
