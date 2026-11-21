package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.application.port.RolRepository;

public interface RolRepositoryJpa extends JpaRepository<Rol, UUID>,
        RolRepository {

    Optional<Rol> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

}