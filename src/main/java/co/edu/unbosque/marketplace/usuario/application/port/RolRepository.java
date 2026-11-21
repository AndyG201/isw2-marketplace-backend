package co.edu.unbosque.marketplace.usuario.application.port;

import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.usuario.domain.Rol;

public interface RolRepository extends RepositorioBasico<Rol, UUID> {
    Optional<Rol> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCase(String nombre);
}
