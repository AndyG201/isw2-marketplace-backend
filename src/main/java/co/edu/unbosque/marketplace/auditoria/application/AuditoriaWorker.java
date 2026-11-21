package co.edu.unbosque.marketplace.auditoria.application;

import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.trabajo.ProcesadorTrabajo;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Component
@Profile("worker")
@ConditionalOnProperty(prefix = "app.auditoria.b2", name = "enabled", havingValue = "true")
public class AuditoriaWorker implements ProcesadorTrabajo {

    private final AuditoriaOutboxService outbox;
    private final AlmacenAuditoriaInmutableJdbc almacen;

    public AuditoriaWorker(
            AuditoriaOutboxService outbox,
            AlmacenAuditoriaInmutableJdbc almacen) {
        this.outbox = outbox;
        this.almacen = almacen;
    }

    @Override
    public boolean soporta(TipoTrabajo tipo) {
        return tipo == TipoTrabajo.COPIAR_AUDITORIA;
    }

    @Override
    public void procesar(TipoTrabajo tipo, String identificador) {
        if (!soporta(tipo)) {
            throw new FallaInternaException("Tipo de trabajo no soportado por el worker de auditoría.");
        }

        UUID idEvento;
        try {
            idEvento = UUID.fromString(identificador);
        } catch (RuntimeException error) {
            throw new DatosInvalidosException("El identificador del evento de auditoría no es un UUID.", error);
        }

        BandejaSalidaAuditoria evento = outbox.buscarParaProcesar(idEvento);
        if (evento == null || "ENVIADO".equals(evento.getEstadoEnvio())) {
            return;
        }

        try {
            almacen.anexar(evento);
            outbox.marcarEnviado(idEvento);
        } catch (Exception error) {
            outbox.marcarError(idEvento, error);
        }
    }
}