
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

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.BandejaSalidaAuditoriaRepository;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.publicacion.api.response.EnviarRevisionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@Service
public class PublicacionRevisionService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoRepository alojamientoRepository;
    private final ImagenPublicacionRepository imagenRepository;
    private final PublicacionServicioRepository publicacionServicioRepository;
    private final ServicioRepository servicioRepository;
    private final PoliticaCancelacionRepository politicaRepository;
    private final BandejaSalidaAuditoriaRepository auditoriaRepository;

    public PublicacionRevisionService(
            PublicacionRepository publicacionRepository,
            AlojamientoRepository alojamientoRepository,
            ImagenPublicacionRepository imagenRepository,
            PublicacionServicioRepository publicacionServicioRepository,
            ServicioRepository servicioRepository,
            PoliticaCancelacionRepository politicaRepository,
            BandejaSalidaAuditoriaRepository auditoriaRepository) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoRepository = alojamientoRepository;
        this.imagenRepository = imagenRepository;
        this.publicacionServicioRepository = publicacionServicioRepository;
        this.servicioRepository = servicioRepository;
        this.politicaRepository = politicaRepository;
        this.auditoriaRepository = auditoriaRepository;
    }

    @Transactional
    public EnviarRevisionResponse enviarRevision(
            Usuario anfitrion,
            UUID idPublicacion) {

        if (anfitrion == null || anfitrion.getIdUsuario() == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para realizar esta operación.");
        }

        if (anfitrion.getRol() == null
                || !"ANFITRION".equalsIgnoreCase(
                        anfitrion.getRol().getNombre())) {

            throw new AccessDeniedException(
                    "Solo un anfitrión puede enviar publicaciones a revisión.");
        }

        if (!anfitrion.isCorreoVerificado()
                || anfitrion.getEstado() != EstadoUsuario.ACTIVA
                || anfitrion.isRestringido()
                || anfitrion.getIntentosFallidos() >= 5) {

            throw new AccessDeniedException(
                    "Tu cuenta no está habilitada para enviar publicaciones a revisión.");
        }

        Publicacion publicacion = publicacionRepository
                .findById(idPublicacion)
                .orElseThrow(() ->
                        new java.util.NoSuchElementException(
                                "La publicación no existe."));

        Alojamiento alojamiento = alojamientoRepository
                .findById(publicacion.getIdAlojamiento())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se encontró el alojamiento asociado."));

        if (!anfitrion.getIdUsuario().equals(
                alojamiento.getIdAnfitrion())) {

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

        registrarEventoAuditoria(
                anfitrion.getIdUsuario(),
                publicacion.getIdPublicacion(),
                ahora
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
            Alojamiento alojamiento) {

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

        if (alojamiento.getUbicacion() == null
                || alojamiento.getUbicacion().isBlank()) {
            faltantes.add("ubicación");
        }

        if (alojamiento.getCiudad() == null
                || alojamiento.getCiudad().isBlank()) {
            faltantes.add("ciudad");
        }

        if (alojamiento.getTipoAlojamiento() == null
                || alojamiento.getTipoAlojamiento().isBlank()) {
            faltantes.add("tipo de alojamiento");
        }

        if (alojamiento.getCapacidad() <= 0) {
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
                || politicaRepository.findByIdPoliticaAndActivaTrue(
                        publicacion.getIdPolitica()).isEmpty()) {
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

            if (servicioRepository.findAllById(idsServicios).size()
                    != idsServicios.size()) {
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
                    + String.join(", ", faltantes) + "."
            );
        }
    }

    private void registrarEventoAuditoria(
            UUID idUsuario,
            UUID idPublicacion,
            OffsetDateTime fecha) {

        BandejaSalidaAuditoria evento =
                new BandejaSalidaAuditoria();

        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(idUsuario);
        evento.setFechaEvento(fecha);
        evento.setAccion("ENVIO_PUBLICACION_REVISION");
        evento.setResultado("EXITOSO");
        evento.setEntidadAfectada("PUBLICACION");
        evento.setIdEntidadAfectada(idPublicacion);
        evento.setDetalle(null);
        evento.setIdCorrelacion(UUID.randomUUID().toString());
        evento.setEstadoEnvio("PENDIENTE");
        evento.setIntentosEnvio(0);
        evento.setProximaEjecucion(fecha);
        evento.setFechaEnvio(null);
        evento.setUltimoError(null);

        auditoriaRepository.save(evento);
    }
}
