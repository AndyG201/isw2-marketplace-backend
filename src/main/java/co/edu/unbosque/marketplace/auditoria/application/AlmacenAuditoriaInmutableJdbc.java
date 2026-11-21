package co.edu.unbosque.marketplace.auditoria.application;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.AuditoriaIndiceJdbcRepository;
import co.edu.unbosque.marketplace.auditoria.infrastructure.storage.BackblazeAuditoriaService;
import co.edu.unbosque.marketplace.auditoria.infrastructure.storage.ResultadoAlmacenamientoAuditoria;

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
public class AlmacenAuditoriaInmutableJdbc {

    private final BackblazeAuditoriaService backblazeAuditoriaService;
    private final AuditoriaIndiceJdbcRepository indiceRepository;

    public AlmacenAuditoriaInmutableJdbc(
            BackblazeAuditoriaService backblazeAuditoriaService,
            AuditoriaIndiceJdbcRepository indiceRepository) {

        this.backblazeAuditoriaService = backblazeAuditoriaService;
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
                backblazeAuditoriaService.almacenar(evento);

        indiceRepository.registrar(evento, resultado);
    }
}
