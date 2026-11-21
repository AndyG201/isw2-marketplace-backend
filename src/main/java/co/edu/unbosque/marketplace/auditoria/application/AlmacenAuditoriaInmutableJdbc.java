package co.edu.unbosque.marketplace.auditoria.application;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.auditoria.domain.AlmacenamientoInmutableAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.ResultadoAlmacenamientoAuditoria;
import co.edu.unbosque.marketplace.auditoria.application.port.AuditoriaIndiceRepository;

/**
 * Coordina el almacenamiento de eventos de auditoría.
 *
 * Primero archiva el evento en Backblaze B2 y posteriormente
 * registra su índice en PostgreSQL.
 *
 * El Worker marca el evento como enviado únicamente cuando
 * ambas operaciones terminan correctamente.
 */
@Component
@Profile("worker")
@ConditionalOnProperty(prefix = "app.auditoria.b2", name = "enabled", havingValue = "true")
public class AlmacenAuditoriaInmutableJdbc {

    private final AlmacenamientoInmutableAuditoria almacenamiento;
    private final AuditoriaIndiceRepository indiceRepository;

    public AlmacenAuditoriaInmutableJdbc(
            AlmacenamientoInmutableAuditoria almacenamiento,
            AuditoriaIndiceRepository indiceRepository) {

        this.almacenamiento = almacenamiento;
        this.indiceRepository = indiceRepository;
    }

    /**
     * Archiva un evento y registra su índice.
     *
     * @param evento evento pendiente de procesamiento.
     */
    public void anexar(BandejaSalidaAuditoria evento) {

        if (evento == null) {
            throw new IllegalArgumentException(
                    "El evento de auditoría no puede ser nulo.");
        }

        ResultadoAlmacenamientoAuditoria resultado =
                almacenamiento.almacenar(evento);

        indiceRepository.registrar(evento, resultado);
    }
}
