
package co.edu.unbosque.marketplace.publicacion.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaPublicacionCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoPublicacion;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.EnviarRevisionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@Service
public class PublicacionRevisionService {

    private final PublicacionRepository publicacionRepository;
    private final ImagenPublicacionRepository imagenRepository;
    private final PublicacionServicioRepository publicacionServicioRepository;

    private final UsuarioPublicacionQuery usuarioPublicacionQuery;
    private final AlojamientoPublicacionQuery alojamientoPublicacionQuery;
    private final ServicioPublicacionQuery servicioPublicacionQuery;
    private final PoliticaCancelacionQuery politicaCancelacionQuery;
    private final AuditoriaPublicacionCommand auditoriaPublicacionCommand;

    public PublicacionRevisionService(
            PublicacionRepository publicacionRepository,
            ImagenPublicacionRepository imagenRepository,
            PublicacionServicioRepository publicacionServicioRepository,
            UsuarioPublicacionQuery usuarioPublicacionQuery,
            AlojamientoPublicacionQuery alojamientoPublicacionQuery,
            ServicioPublicacionQuery servicioPublicacionQuery,
            PoliticaCancelacionQuery politicaCancelacionQuery,
            AuditoriaPublicacionCommand auditoriaPublicacionCommand) {

        this.publicacionRepository = publicacionRepository;
        this.imagenRepository = imagenRepository;
        this.publicacionServicioRepository = publicacionServicioRepository;
        this.usuarioPublicacionQuery = usuarioPublicacionQuery;
        this.alojamientoPublicacionQuery = alojamientoPublicacionQuery;
        this.servicioPublicacionQuery = servicioPublicacionQuery;
        this.politicaCancelacionQuery = politicaCancelacionQuery;
        this.auditoriaPublicacionCommand = auditoriaPublicacionCommand;
    }

    @Transactional
    public EnviarRevisionResponse enviarRevision(
            UUID idAnfitrion,
            UUID idPublicacion) {

        if (idAnfitrion == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación.");
        }

        UsuarioPublicacionInfo anfitrion = usuarioPublicacionQuery
                .consultarParaPublicacion(idAnfitrion)
                .orElseThrow(() ->
                        new AccessDeniedException(
                                "Debes iniciar sesión para realizar esta operación."));

        if (!anfitrion.esAnfitrion()) {
            throw new AccessDeniedException(
                    "Solo un anfitrión puede enviar publicaciones a revisión.");
        }

        if (!anfitrion.cuentaHabilitadaParaPublicar()) {
            throw new AccessDeniedException(
                    "Tu cuenta no está habilitada para enviar publicaciones a revisión.");
        }

        Publicacion publicacion = publicacionRepository
                .findById(idPublicacion)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "La publicación no existe."));

        AlojamientoPublicacionInfo alojamiento = alojamientoPublicacionQuery
                .consultarDatosParaPublicacion(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado."));

        if (!idAnfitrion.equals(alojamiento.idAnfitrion())) {
            throw new AccessDeniedException(
                    "No puedes enviar a revisión una publicación que no te pertenece.");
        }

        String estadoActual = publicacion.getEstado();

        if ("BLOQUEADA".equals(estadoActual)) {
            throw new IllegalStateException(
                    "No puedes enviar a revisión una publicación bloqueada.");
        }

        if (!"BORRADOR".equals(estadoActual)) {
            throw new IllegalStateException(
                    "Solamente las publicaciones en estado BORRADOR pueden enviarse a revisión.");
        }

        validarInformacionMinima(publicacion, alojamiento);

        OffsetDateTime ahora = OffsetDateTime.now();

        publicacion.setEstado("PENDIENTE_REVISION");
        publicacion.setFechaActualizacion(ahora);

        publicacionRepository.save(publicacion);

        auditoriaPublicacionCommand.registrarEvento(
                new RegistrarEventoPublicacion(
                        idAnfitrion,
                        publicacion.getIdPublicacion(),
                        ahora,
                        "ENVIO_PUBLICACION_REVISION",
                        "EXITOSO",
                        null
                )
        );

        return new EnviarRevisionResponse(
                publicacion.getIdPublicacion(),
                estadoActual,
                publicacion.getEstado(),
                "La publicación ha sido enviada a revisión correctamente.",
                ahora
        );
    }

    private void validarInformacionMinima(
            Publicacion publicacion,
            AlojamientoPublicacionInfo alojamiento) {

        List<String> faltantes = new ArrayList<>();

        if (publicacion.getTitulo() == null
                || publicacion.getTitulo().isBlank()) {
            faltantes.add("título");
        }

        if (publicacion.getDescripcion() == null
                || publicacion.getDescripcion().isBlank()) {
            faltantes.add("descripción");
        }

        if (publicacion.getReglas() == null
                || publicacion.getReglas().isBlank()) {
            faltantes.add("reglas");
        }

        if (alojamiento.ubicacion() == null
                || alojamiento.ubicacion().isBlank()) {
            faltantes.add("ubicación");
        }

        if (alojamiento.ciudad() == null
                || alojamiento.ciudad().isBlank()) {
            faltantes.add("ciudad");
        }

        if (alojamiento.tipoAlojamiento() == null
                || alojamiento.tipoAlojamiento().isBlank()) {
            faltantes.add("tipo de alojamiento");
        }

        if (alojamiento.capacidad() <= 0) {
            faltantes.add("capacidad válida");
        }

        BigDecimal precio = publicacion.getPrecioPorNoche();

        if (precio == null || precio.signum() <= 0) {
            faltantes.add("precio por noche válido");
        }

        BigDecimal limpieza = publicacion.getTarifaLimpieza();

        if (limpieza == null || limpieza.signum() < 0) {
            faltantes.add("tarifa de limpieza válida");
        }

        String modo = publicacion.getModoAceptacion();

        if (modo == null
                || !List.of("INMEDIATA", "APROBACION")
                        .contains(modo.trim().toUpperCase(Locale.ROOT))) {
            faltantes.add("modo de aceptación válido");
        }

        if (publicacion.getIdPolitica() == null
                || !politicaCancelacionQuery.existePoliticaActiva(
                        publicacion.getIdPolitica())) {
            faltantes.add("política de cancelación activa");
        }

        List<PublicacionServicio> relaciones =
                publicacionServicioRepository.findByIdPublicacion(
                        publicacion.getIdPublicacion());

        if (relaciones.isEmpty()) {
            faltantes.add("al menos un servicio");
        } else {

            List<UUID> idsServicios = relaciones.stream()
                    .map(PublicacionServicio::getIdServicio)
                    .toList();

            List<ServicioInfo> servicios =
                    servicioPublicacionQuery.consultarPorIds(idsServicios);

            if (servicios.size() != idsServicios.size()) {
                faltantes.add("servicios válidos");
            }
        }

        List<ImagenPublicacion> imagenes =
                imagenRepository.findByIdPublicacionOrderByOrdenAsc(
                        publicacion.getIdPublicacion());

        if (imagenes.isEmpty()) {
            faltantes.add("al menos una imagen");
        } else if (imagenes.stream().anyMatch(imagen ->
                imagen.getRutaImagen() == null
                        || imagen.getRutaImagen().isBlank())) {
            faltantes.add("rutas de imágenes válidas");
        }

        if (!faltantes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No puedes enviar la publicación a revisión. "
                    + "Información faltante o inválida: "
                    + String.join(", ", faltantes) + ".");
        }
    }
}
