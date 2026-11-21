package co.edu.unbosque.marketplace.resena.application.port;

import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.resena.domain.Resena;

public interface ResenaPublicaQueryRepository {

    boolean existePublicacion(UUID idPublicacion);

    List<Resena> consultarVisibles(UUID idPublicacion);
}
