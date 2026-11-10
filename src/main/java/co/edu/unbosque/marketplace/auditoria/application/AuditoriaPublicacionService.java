
package co.edu.unbosque.marketplace.auditoria.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaPublicacionCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoPublicacion;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.BandejaSalidaAuditoriaRepository;

@Service
public class AuditoriaPublicacionService
        implements AuditoriaPublicacionCommand {

    private final BandejaSalidaAuditoriaRepository auditoriaRepository;

    public AuditoriaPublicacionService(
            BandejaSalidaAuditoriaRepository auditoriaRepository) {

        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarEvento(
            RegistrarEventoPublicacion solicitud) {

        validarSolicitud(solicitud);

        BandejaSalidaAuditoria evento =
                new BandejaSalidaAuditoria();

        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(solicitud.idUsuario());
        evento.setFechaEvento(solicitud.fechaEvento());
        evento.setAccion(solicitud.accion());
        evento.setResultado(solicitud.resultado());
        evento.setEntidadAfectada("PUBLICACION");
        evento.setIdEntidadAfectada(solicitud.idPublicacion());
        evento.setDetalle(solicitud.detalle());
        evento.setIdCorrelacion(UUID.randomUUID().toString());
        evento.setEstadoEnvio("PENDIENTE");
        evento.setIntentosEnvio(0);
        evento.setProximaEjecucion(solicitud.fechaEvento());
        evento.setFechaEnvio(null);
        evento.setUltimoError(null);

        auditoriaRepository.save(evento);
    }

    private void validarSolicitud(
            RegistrarEventoPublicacion solicitud) {

        if (solicitud == null
                || solicitud.idPublicacion() == null
                || solicitud.fechaEvento() == null
                || solicitud.accion() == null
                || solicitud.accion().isBlank()
                || solicitud.resultado() == null
                || solicitud.resultado().isBlank()) {

            throw new IllegalArgumentException(
                    "Los datos del evento de auditoría son obligatorios.");
        }
    }
}
