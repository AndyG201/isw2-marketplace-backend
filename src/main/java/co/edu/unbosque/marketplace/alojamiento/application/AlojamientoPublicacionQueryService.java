
package co.edu.unbosque.marketplace.alojamiento.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.alojamiento.infrastructure.persistence.AlojamientoRepository;

@Service
public class AlojamientoPublicacionQueryService implements AlojamientoPublicacionQuery {

	private final AlojamientoRepository alojamientoRepository;

	public AlojamientoPublicacionQueryService(AlojamientoRepository alojamientoRepository) {

		this.alojamientoRepository = alojamientoRepository;
	}

	@Override
	public Optional<UUID> consultarIdAnfitrion(UUID idAlojamiento) {

		if (idAlojamiento == null) {
			return Optional.empty();
		}

		return alojamientoRepository.findById(idAlojamiento).map(alojamiento -> alojamiento.getIdAnfitrion());
	}

	@Override
	public Optional<AlojamientoPublicacionInfo> consultarDatosParaPublicacion(UUID idAlojamiento) {

		if (idAlojamiento == null) {
			return Optional.empty();
		}

		return alojamientoRepository.findById(idAlojamiento)
				.map(alojamiento -> new AlojamientoPublicacionInfo(alojamiento.getIdAlojamiento(),
						alojamiento.getIdAnfitrion(), alojamiento.getUbicacion(), alojamiento.getCiudad(),
						alojamiento.getTipoAlojamiento(), alojamiento.getCapacidad()));
	}

	@Override
	public List<AlojamientoPublicacionInfo> consultarPorAnfitrion(UUID idAnfitrion) {

		if (idAnfitrion == null) {
			return List.of();
		}

		return alojamientoRepository.findByIdAnfitrion(idAnfitrion).stream()
				.map(alojamiento -> new AlojamientoPublicacionInfo(alojamiento.getIdAlojamiento(),
						alojamiento.getIdAnfitrion(), alojamiento.getUbicacion(), alojamiento.getCiudad(),
						alojamiento.getTipoAlojamiento(), alojamiento.getCapacidad()))
				.toList();
	}

}
