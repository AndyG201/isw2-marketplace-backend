
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionInfo;
import co.edu.unbosque.marketplace.cancelacion.contracts.PoliticaCancelacionQuery;
import co.edu.unbosque.marketplace.publicacion.api.response.PoliticaCancelacionPublicacionResponse;
import co.edu.unbosque.marketplace.publicacion.domain.Publicacion;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;

@Service
public class PublicacionPoliticaCancelacionService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;
    private final PoliticaCancelacionQuery politicaQuery;

    public PublicacionPoliticaCancelacionService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery,
            PoliticaCancelacionQuery politicaQuery) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
        this.politicaQuery = politicaQuery;
    }

    @Transactional
    public PoliticaCancelacionPublicacionResponse seleccionarPolitica(
            UUID idAnfitrion,
            UUID idPublicacion,
            UUID idPolitica) {

        validarIdentificadores(idAnfitrion, idPublicacion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarPropietario(idAnfitrion, publicacion);

        PoliticaCancelacionInfo politica = politicaQuery
                .consultarPoliticaSeleccionable(idPolitica)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "La política seleccionada no existe, está inactiva o no es una opción permitida."
                        ));

        publicacion.setIdPolitica(politica.idPolitica());
        publicacion.setFechaActualizacion(OffsetDateTime.now());

        publicacionRepository.save(publicacion);

        return construirRespuesta(
                publicacion.getIdPublicacion(),
                politica
        );
    }

    @Transactional(readOnly = true)
    public PoliticaCancelacionPublicacionResponse consultarPolitica(
            UUID idAnfitrion,
            UUID idPublicacion) {

        validarIdentificadores(idAnfitrion, idPublicacion);

        Publicacion publicacion = obtenerPublicacion(idPublicacion);

        validarPropietario(idAnfitrion, publicacion);

        PoliticaCancelacionInfo politica = politicaQuery
                .consultarPorId(publicacion.getIdPolitica())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "La política asociada a la publicación no existe."
                        ));

        return construirRespuesta(
                publicacion.getIdPublicacion(),
                politica
        );
    }

    private Publicacion obtenerPublicacion(UUID idPublicacion) {

        return publicacionRepository.findById(idPublicacion)
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "La publicación no existe."
                        ));
    }

    private void validarPropietario(
            UUID idAnfitrion,
            Publicacion publicacion) {

        AlojamientoPublicacionInfo alojamiento = alojamientoQuery
                .consultarDatosParaPublicacion(
                        publicacion.getIdAlojamiento()
                )
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No se encontró el alojamiento asociado a la publicación."
                        ));

        if (!idAnfitrion.equals(alojamiento.idAnfitrion())) {

            throw new AccessDeniedException(
                    "No tienes permisos para modificar esta publicación."
            );
        }
    }

    private void validarIdentificadores(
            UUID idAnfitrion,
            UUID idPublicacion) {

        if (idAnfitrion == null) {
            throw new AccessDeniedException(
                    "Debes iniciar sesión para configurar la política de cancelación."
            );
        }

        if (idPublicacion == null) {
            throw new IllegalArgumentException(
                    "El identificador de la publicación es obligatorio."
            );
        }
    }

    private PoliticaCancelacionPublicacionResponse construirRespuesta(
            UUID idPublicacion,
            PoliticaCancelacionInfo politica) {

        return new PoliticaCancelacionPublicacionResponse(
                idPublicacion,
                politica.idPolitica(),
                politica.nombre(),
                politica.version(),
                politica.descripcion(),
                politica.anticipacionHoras(),
                politica.porcentajeDevolucion(),
                politica.activa()
        );
    }
}
