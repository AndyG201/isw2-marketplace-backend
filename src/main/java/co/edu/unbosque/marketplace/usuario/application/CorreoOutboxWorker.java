package co.edu.unbosque.marketplace.usuario.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.usuario.domain.ServicioCorreo;

@Component
@Profile("worker")
public class CorreoOutboxWorker {
    private static final Logger LOG = LoggerFactory.getLogger(CorreoOutboxWorker.class);
    private final CorreoOutboxService outbox;
    private final ServicioCorreo correo;

    public CorreoOutboxWorker(CorreoOutboxService outbox, ServicioCorreo correo) {
        this.outbox = outbox;
        this.correo = correo;
    }

    @Scheduled(fixedDelayString = "${app.usuario.correo-outbox.intervalo-ms:5000}",
               initialDelayString = "${app.usuario.correo-outbox.inicio-ms:5000}")
    public void procesarPendientes() {
        for (CorreoOutboxService.CorreoParaEnviar item : outbox.reclamarPendientes(25)) {
            try {
                if (!"VERIFICACION".equals(item.tipo())) {
                    throw new IllegalStateException("Tipo de correo no soportado: " + item.tipo());
                }
                correo.enviarCodigoVerificacion(item.destinatario(), item.codigo());
                outbox.marcarEnviado(item.id());
            } catch (Exception error) {
                LOG.warn("No se pudo enviar correo outbox {}. Se programará reintento.", item.id(), error);
                outbox.marcarError(item.id(), error);
            }
        }
    }
}
