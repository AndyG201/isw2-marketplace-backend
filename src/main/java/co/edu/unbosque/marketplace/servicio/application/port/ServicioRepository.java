package co.edu.unbosque.marketplace.servicio.application.port;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.servicio.domain.Servicio;

public interface ServicioRepository extends RepositorioBasico<Servicio, UUID> {
    List<Servicio> findAllById(Iterable<UUID> ids);
    List<Servicio> findAll(Sort sort);
}
