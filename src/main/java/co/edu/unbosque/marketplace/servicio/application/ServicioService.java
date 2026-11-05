
package co.edu.unbosque.marketplace.servicio.application;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import co.edu.unbosque.marketplace.servicio.api.response.ServicioResponse;
import co.edu.unbosque.marketplace.servicio.infrastructure.persistence.ServicioRepository;

@Service
public class ServicioService {

    private final ServicioRepository servicioRepository;

    public ServicioService(ServicioRepository servicioRepository) {
        this.servicioRepository = servicioRepository;
    }

    public List<ServicioResponse> consultarServicios() {

        return servicioRepository
                .findAll(Sort.by(Sort.Direction.ASC, "nombre"))
                .stream()
                .map(servicio -> new ServicioResponse(
                        servicio.getIdServicio(),
                        servicio.getNombre(),
                        servicio.getDescripcion()
                ))
                .toList();
    }
}
