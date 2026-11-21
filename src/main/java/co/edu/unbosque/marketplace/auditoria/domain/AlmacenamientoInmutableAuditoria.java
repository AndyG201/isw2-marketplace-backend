package co.edu.unbosque.marketplace.auditoria.domain;

import java.util.UUID;

public interface AlmacenamientoInmutableAuditoria {

    ResultadoAlmacenamientoAuditoria almacenar(BandejaSalidaAuditoria evento);

    EventoAuditoria obtenerEvento(
            String claveObjeto,
            String versionId,
            String hashEsperado,
            UUID idEventoEsperado);
}
