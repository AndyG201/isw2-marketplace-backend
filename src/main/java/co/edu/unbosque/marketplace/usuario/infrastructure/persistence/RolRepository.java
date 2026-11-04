package co.edu.unbosque.marketplace.usuario.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.marketplace.usuario.domain.Rol;

public interface RolRepository extends JpaRepository<Rol, UUID> {

    Optional<Rol> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

}