package co.edu.unbosque.marketplace.auditoria.application.port;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;

public interface AuditoriaConsultaRepository {

    List<EventoAuditoriaIndice> consultar(FiltrosAuditoria filtros, int limite, long desplazamiento);

    long contar(FiltrosAuditoria filtros);

    Optional<EventoAuditoriaIndice> buscar(UUID idEvento);
}
