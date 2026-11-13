package co.edu.unbosque.marketplace.publicacion.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;

@Service
public class PublicacionDisponibilidadQueryService
        implements PublicacionDisponibilidadQuery {

    private static final String ESTADO_ACTIVA = "ACTIVA";

    private final PublicacionRepository publicacionRepository;

    public PublicacionDisponibilidadQueryService(
            PublicacionRepository publicacionRepository) {

        this.publicacionRepository = publicacionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePublicacionActivaPorAlojamiento(
            UUID idAlojamiento) {

        if (idAlojamiento == null) {
            return false;
        }

        return publicacionRepository.findByIdAlojamiento(idAlojamiento)
                .map(publicacion ->
                        ESTADO_ACTIVA.equals(publicacion.getEstado())
                )
                .orElse(false);
    }
}
