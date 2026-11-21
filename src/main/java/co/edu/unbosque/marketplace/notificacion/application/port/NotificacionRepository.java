package co.edu.unbosque.marketplace.notificacion.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;

public interface NotificacionRepository {

    <S extends Notificacion> S save(S entidad);
    <S extends Notificacion> List<S> saveAll(Iterable<S> entidades);
    <S extends Notificacion> S saveAndFlush(S entidad);
    Optional<Notificacion> findById(UUID id);
    List<Notificacion> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(Notificacion entidad);
    void deleteById(UUID id);
    void flush();
    List<Notificacion> findByIdUsuarioOrderByFechaCreacionDesc(UUID idUsuario);
}
