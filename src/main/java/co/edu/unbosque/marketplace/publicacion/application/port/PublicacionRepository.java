package co.edu.unbosque.marketplace.publicacion.application.port;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

public interface PublicacionRepository extends RepositorioBasico<Publicacion, UUID> {
    List<Publicacion> findAll(Sort sort);

    Optional<Publicacion> findByIdAlojamiento(UUID idAlojamiento);
    List<Publicacion> findByIdAlojamientoInOrderByFechaCreacionDesc(Collection<UUID> idsAlojamiento);
    Optional<Publicacion> findByIdPublicacionAndIdAlojamientoIn(UUID idPublicacion, Collection<UUID> idsAlojamiento);
    int pausarPublicacionesActivasPorAlojamientos(Collection<UUID> idsAlojamientos, OffsetDateTime fechaActualizacion);
    List<Publicacion> findByEstadoOrderByFechaCreacionDesc(String estado);
}
