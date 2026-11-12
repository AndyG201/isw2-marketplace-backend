package co.edu.unbosque.marketplace.servicio.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.servicio.domain.ServicioAdicional;

@Repository
public interface ServicioAdicionalRepository
        extends JpaRepository<ServicioAdicional, UUID> {

    List<ServicioAdicional> findByIdPublicacionAndActivoTrueOrderByNombreAsc(
            UUID idPublicacion
    );
}
