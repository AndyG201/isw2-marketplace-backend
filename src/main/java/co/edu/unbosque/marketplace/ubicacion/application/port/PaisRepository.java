package co.edu.unbosque.marketplace.ubicacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.ubicacion.domain.Pais;

public interface PaisRepository extends RepositorioBasico<Pais, UUID> {
    List<Pais> findAllByOrderByNombreAsc();
    Optional<Pais> findByCodigoIso2IgnoreCase(String codigoIso2);
}
