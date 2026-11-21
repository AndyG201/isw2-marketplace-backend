package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.usuario.domain.CorreoPendiente;

public interface CorreoPendienteRepository extends RepositorioBasico<CorreoPendiente, UUID> {
    List<CorreoPendiente> reclamar(OffsetDateTime ahora, org.springframework.data.domain.Pageable pageable);
}
