
package co.edu.unbosque.marketplace.publicacion.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;
import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.publicacion.infrastructure.persistence.PublicacionRepository;

@Service
public class PublicacionCuentaService {

    private final PublicacionRepository publicacionRepository;
    private final AlojamientoPublicacionQuery alojamientoQuery;

    public PublicacionCuentaService(
            PublicacionRepository publicacionRepository,
            AlojamientoPublicacionQuery alojamientoQuery) {

        this.publicacionRepository = publicacionRepository;
        this.alojamientoQuery = alojamientoQuery;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public int pausarPublicacionesActivas(
            UUID idUsuario,
            OffsetDateTime fechaEvento) {

        if (idUsuario == null || fechaEvento == null) {
            throw new IllegalArgumentException(
                    "Los datos del evento de cuenta son obligatorios.");
        }

        List<UUID> idsAlojamientos =
                alojamientoQuery.consultarPorAnfitrion(idUsuario)
                        .stream()
                        .map(AlojamientoPublicacionInfo::idAlojamiento)
                        .toList();

        if (idsAlojamientos.isEmpty()) {
            return 0;
        }

        return publicacionRepository
                .pausarPublicacionesActivasPorAlojamientos(
                        idsAlojamientos,
                        fechaEvento
                );
    }
}
