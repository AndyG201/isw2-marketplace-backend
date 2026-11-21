package co.edu.unbosque.marketplace.auditoria.application;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("worker")
public class ProgramadorAuditoria {

    private final AuditoriaOutboxService outbox;

    public ProgramadorAuditoria(AuditoriaOutboxService outbox) {
        this.outbox = outbox;
    }

    @Scheduled(fixedDelayString = "${app.auditoria.salida.intervalo-ms:2000}")
    public void programarPendientes() {
        outbox.encolarPendientes();
    }
}