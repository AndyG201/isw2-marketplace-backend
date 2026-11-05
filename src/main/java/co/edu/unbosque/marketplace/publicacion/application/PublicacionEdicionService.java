
package co.edu.unbosque.marketplace.publicacion.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;
import co.edu.unbosque.marketplace.cancelacion.infrastructure.persistence.PoliticaCancelacionRepository;
import co.edu.unbosque.marketplace.publicacion.api.request.EditarPublicacionRequest;
import co.edu.unbosque.marketplace.publicacion.api.response.PublicacionAlojamientoResponse;
import co.edu.unbosque.marketplace.publicacion.domain.ImagenPublicacion;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.domain.PublicacionServicio;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.ImagenPublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionServicioRepository;
import co.edu.unbosque.marketplace.publicacion.infrastructure.storage.AlmacenamientoImagenService;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@Service
public class PublicacionEdicionService {

	private static final Logger logger = LoggerFactory.getLogger(PublicacionEdicionService.class);

	private final PublicacionRepository publicacionRepository;
	private final AlojamientoRepository alojamientoRepository;
	private final PublicacionServicioRepository publicacionServicioRepository;
	private final ImagenPublicacionRepository imagenRepository;
	private final ServicioRepository servicioRepository;
	private final PoliticaCancelacionRepository politicaRepository;
	private final AlmacenamientoImagenService almacenamientoImagenService;

	public PublicacionEdicionService(PublicacionRepository publicacionRepository,
			AlojamientoRepository alojamientoRepository, PublicacionServicioRepository publicacionServicioRepository,
			ImagenPublicacionRepository imagenRepository, ServicioRepository servicioRepository,
			PoliticaCancelacionRepository politicaRepository, AlmacenamientoImagenService almacenamientoImagenService) {

		this.publicacionRepository = publicacionRepository;
		this.alojamientoRepository = alojamientoRepository;
		this.publicacionServicioRepository = publicacionServicioRepository;
		this.imagenRepository = imagenRepository;
		this.servicioRepository = servicioRepository;
		this.politicaRepository = politicaRepository;
		this.almacenamientoImagenService = almacenamientoImagenService;
	}

	@Transactional
	public PublicacionAlojamientoResponse actualizarPublicacion(Usuario actor, UUID idPublicacion,
			EditarPublicacionRequest solicitud) {

		validarSolicitud(actor, idPublicacion, solicitud);

		Publicacion publicacion = publicacionRepository.findById(idPublicacion)
				.orElseThrow(() -> new NoSuchElementException("La publicación no existe."));

		Alojamiento alojamiento = alojamientoRepository.findById(publicacion.getIdAlojamiento())
				.orElseThrow(() -> new IllegalStateException("No se encontró el alojamiento asociado."));

		validarPermisos(actor, alojamiento);

		var politica = politicaRepository.findByIdPoliticaAndActivaTrue(solicitud.getIdPolitica())
				.orElseThrow(() -> new IllegalArgumentException("La política seleccionada no existe o está inactiva."));

		Set<UUID> idsServicios = new HashSet<>(solicitud.getIdsServicios());

		if (idsServicios.size() != solicitud.getIdsServicios().size()) {
			throw new IllegalArgumentException("No puedes seleccionar un mismo servicio varias veces.");
		}

		var servicios = servicioRepository.findAllById(idsServicios);

		if (servicios.size() != idsServicios.size()) {
			throw new IllegalArgumentException("Uno o varios servicios seleccionados no existen.");
		}

		OffsetDateTime ahora = OffsetDateTime.now();

		alojamiento.setUbicacion(solicitud.getUbicacion().trim());
		alojamiento.setCiudad(solicitud.getCiudad().trim());
		alojamiento.setTipoAlojamiento(solicitud.getTipoAlojamiento().trim());
		alojamiento.setCapacidad(solicitud.getCapacidad());

		publicacion.setTitulo(solicitud.getTitulo().trim());
		publicacion.setDescripcion(solicitud.getDescripcion().trim());
		publicacion.setReglas(solicitud.getReglas().trim());
		publicacion.setPrecioPorNoche(solicitud.getPrecioPorNoche());
		publicacion.setTarifaLimpieza(
				solicitud.getTarifaLimpieza() == null ? BigDecimal.ZERO : solicitud.getTarifaLimpieza());
		publicacion.setModoAceptacion(solicitud.getModoAceptacion().trim().toUpperCase(Locale.ROOT));
		publicacion.setIdPolitica(politica.getIdPolitica());
		publicacion.setFechaActualizacion(ahora);

		alojamientoRepository.save(alojamiento);
		publicacionRepository.save(publicacion);

		actualizarServicios(publicacion.getIdPublicacion(), idsServicios);

		List<ImagenPublicacion> imagenesActuales = imagenRepository.findByIdPublicacionOrderByOrdenAsc(idPublicacion);

		List<String> rutasImagenes;

		if (solicitud.getImagenes() != null && !solicitud.getImagenes().isEmpty()) {

			rutasImagenes = reemplazarImagenes(idPublicacion, solicitud.getImagenes(), imagenesActuales);

		} else {

			rutasImagenes = imagenesActuales.stream().map(ImagenPublicacion::getRutaImagen).toList();
		}

		return new PublicacionAlojamientoResponse(alojamiento.getIdAlojamiento(), publicacion.getIdPublicacion(),
				publicacion.getTitulo(), publicacion.getEstado(), rutasImagenes);
	}

	private void validarPermisos(Usuario actor, Alojamiento alojamiento) {

		String rol = actor.getRol() == null ? "" : actor.getRol().getNombre().trim().toUpperCase(Locale.ROOT);

		if ("ADMINISTRADOR".equals(rol)) {
			return;
		}

		if ("ANFITRION".equals(rol) && alojamiento.getIdAnfitrion().equals(actor.getIdUsuario())) {
			return;
		}

		throw new AccessDeniedException("No tienes permisos para modificar esta publicación.");
	}

	private void validarSolicitud(Usuario actor, UUID idPublicacion, EditarPublicacionRequest solicitud) {

		if (actor == null || actor.getIdUsuario() == null) {
			throw new AccessDeniedException("Debes iniciar sesión para modificar publicaciones.");
		}

		if (idPublicacion == null || solicitud == null) {
			throw new IllegalArgumentException("La solicitud de actualización no es válida.");
		}

		if (solicitud.getCapacidad() == null || solicitud.getCapacidad() <= 0) {
			throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
		}

		if (solicitud.getPrecioPorNoche() == null || solicitud.getPrecioPorNoche().signum() <= 0) {
			throw new IllegalArgumentException("El precio por noche debe ser mayor que cero.");
		}

		if (solicitud.getTarifaLimpieza() != null && solicitud.getTarifaLimpieza().signum() < 0) {
			throw new IllegalArgumentException("La tarifa de limpieza no puede ser negativa.");
		}

		if (solicitud.getIdsServicios() == null || solicitud.getIdsServicios().isEmpty()
				|| solicitud.getIdsServicios().size() > 20 || solicitud.getIdsServicios().contains(null)) {
			throw new IllegalArgumentException("Debes seleccionar entre 1 y 20 servicios válidos.");
		}

		if (solicitud.getIdPolitica() == null) {
			throw new IllegalArgumentException("Debes seleccionar una política de cancelación.");
		}

		if (solicitud.getImagenes() != null && solicitud.getImagenes().size() > 10) {
			throw new IllegalArgumentException("No puedes cargar más de 10 imágenes.");
		}
	}

	private void actualizarServicios(UUID idPublicacion, Set<UUID> idsNuevos) {

		List<PublicacionServicio> actuales = publicacionServicioRepository.findByIdPublicacion(idPublicacion);

		Set<UUID> idsActuales = actuales.stream().map(PublicacionServicio::getIdServicio)
				.collect(java.util.stream.Collectors.toSet());

		List<PublicacionServicio> eliminar = actuales.stream()
				.filter(relacion -> !idsNuevos.contains(relacion.getIdServicio())).toList();

		List<PublicacionServicio> agregar = idsNuevos.stream().filter(id -> !idsActuales.contains(id))
				.map(id -> new PublicacionServicio(idPublicacion, id)).toList();

		if (!eliminar.isEmpty()) {
			publicacionServicioRepository.deleteAll(eliminar);
		}

		if (!agregar.isEmpty()) {
			publicacionServicioRepository.saveAll(agregar);
		}
	}

	private List<String> reemplazarImagenes(UUID idPublicacion,
			List<org.springframework.web.multipart.MultipartFile> archivos,
			List<ImagenPublicacion> imagenesAnteriores) {

		List<String> rutasNuevas = new ArrayList<>();

		List<String> rutasAnteriores = imagenesAnteriores.stream().map(ImagenPublicacion::getRutaImagen).toList();

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

			@Override
			public void afterCompletion(int status) {

				if (status == STATUS_ROLLED_BACK) {
					eliminarArchivos(rutasNuevas);
				}

				if (status == STATUS_COMMITTED) {
					eliminarArchivos(rutasAnteriores);
				}
			}
		});

		List<ImagenPublicacion> nuevasImagenes = new ArrayList<>();

		for (int i = 0; i < archivos.size(); i++) {

			String ruta = almacenamientoImagenService.guardar(archivos.get(i));

			rutasNuevas.add(ruta);

			ImagenPublicacion imagen = new ImagenPublicacion();

			imagen.setIdImagen(UUID.randomUUID());
			imagen.setIdPublicacion(idPublicacion);
			imagen.setRutaImagen(ruta);
			imagen.setOrden(i + 1);
			imagen.setEsPortada(i == 0);

			nuevasImagenes.add(imagen);
		}

		imagenRepository.deleteAll(imagenesAnteriores);

		imagenRepository.flush();

		imagenRepository.saveAll(nuevasImagenes);

		return rutasNuevas;
	}

	private void eliminarArchivos(List<String> rutas) {

		for (String ruta : rutas) {
			try {
				almacenamientoImagenService.eliminar(ruta);
			} catch (RuntimeException e) {
				logger.warn("No fue posible eliminar el archivo de imagen: {}", ruta, e);
			}
		}
	}
}
