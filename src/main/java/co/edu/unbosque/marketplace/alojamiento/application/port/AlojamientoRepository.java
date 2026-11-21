package co.edu.unbosque.marketplace.alojamiento.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.alojamiento.domain.Alojamiento;

public interface AlojamientoRepository extends RepositorioBasico<Alojamiento, UUID> {
    List<Alojamiento> findAllById(Iterable<UUID> ids);

    List<Alojamiento> findByIdAnfitrion(UUID idAnfitrion);
}
