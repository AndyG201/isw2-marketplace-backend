package co.edu.unbosque.marketplace.auditoria.application.port;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.ResultadoAlmacenamientoAuditoria;

public interface AuditoriaIndiceRepository {

    void registrar(BandejaSalidaAuditoria evento, ResultadoAlmacenamientoAuditoria almacenamiento);
}
