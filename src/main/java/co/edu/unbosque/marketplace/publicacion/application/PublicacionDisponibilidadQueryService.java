package co.edu.unbosque.marketplace.publicacion.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.EstadoPublicacion;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDisponibilidadQuery;

@Service
public class PublicacionDisponibilidadQueryService
        implements PublicacionDisponibilidadQuery {


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
                        publicacion.estaEn(EstadoPublicacion.ACTIVA)
                )
                .orElse(false);
    }
}
