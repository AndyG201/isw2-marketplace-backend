package co.edu.unbosque.marketplace.servicio.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.servicio.domain.ServicioAdicional;
import co.edu.unbosque.marketplace.servicio.application.port.ServicioAdicionalRepository;

@Repository
public interface ServicioAdicionalRepositoryJpa extends JpaRepository<ServicioAdicional, UUID>,
        ServicioAdicionalRepository {

    List<ServicioAdicional> findByIdPublicacionAndActivoTrueOrderByNombreAsc(
            UUID idPublicacion
    );
}
