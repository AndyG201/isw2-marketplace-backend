package co.edu.unbosque.marketplace.publicacion.application.port;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

public interface PublicacionRepository {

    <S extends Publicacion> S save(S entidad);
    <S extends Publicacion> List<S> saveAll(Iterable<S> entidades);
    <S extends Publicacion> S saveAndFlush(S entidad);
    Optional<Publicacion> findById(UUID id);
    List<Publicacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Publicacion entidad);
    void deleteById(UUID id);
    void flush();
    List<Publicacion> findAll(Sort sort);

    Optional<Publicacion> findByIdAlojamiento(UUID idAlojamiento);
    List<Publicacion> findByIdAlojamientoInOrderByFechaCreacionDesc(Collection<UUID> idsAlojamiento);
    Optional<Publicacion> findByIdPublicacionAndIdAlojamientoIn(UUID idPublicacion, Collection<UUID> idsAlojamiento);
    int pausarPublicacionesActivasPorAlojamientos(Collection<UUID> idsAlojamientos, OffsetDateTime fechaActualizacion);
    List<Publicacion> findByEstadoOrderByFechaCreacionDesc(String estado);
}
