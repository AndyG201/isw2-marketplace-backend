package co.edu.unbosque.marketplace.publicacion.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.publicacion.application.port.PublicacionRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionAlojamientoQuery;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;

@Service
public class PublicacionAlojamientoQueryService implements PublicacionAlojamientoQuery {

    private final PublicacionRepository publicaciones;

    public PublicacionAlojamientoQueryService(PublicacionRepository publicaciones) {
        this.publicaciones = publicaciones;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UUID> consultarIdAlojamiento(UUID idPublicacion) {
        if (idPublicacion == null) {
            return Optional.empty();
        }
        return publicaciones.findById(idPublicacion).map(Publicacion::getIdAlojamiento);
    }
}
