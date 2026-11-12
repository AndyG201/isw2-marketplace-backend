
package co.edu.unbosque.marketplace.servicio.application;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.servicio.contracts.ServicioInfo;
import co.edu.unbosque.marketplace.servicio.contracts.ServicioPublicacionQuery;
import co.edu.unbosque.marketplace.servicio.domain.Servicio;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;

@Service
public class ServicioPublicacionQueryService
        implements ServicioPublicacionQuery {

    private final ServicioRepository servicioRepository;

    public ServicioPublicacionQueryService(
            ServicioRepository servicioRepository) {

        this.servicioRepository = servicioRepository;
    }

    @Override
    public List<ServicioInfo> consultarPorIds(
            Collection<UUID> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        List<Servicio> servicios =
                servicioRepository.findAllById(ids);

        return servicios.stream()
                .map(servicio -> new ServicioInfo(
                        servicio.getIdServicio(),
                        servicio.getNombre(),
                        servicio.getDescripcion()
                ))
                .toList();
    }
}
