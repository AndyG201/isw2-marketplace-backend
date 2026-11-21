package co.edu.unbosque.marketplace.auditoria.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.application.port.BandejaSalidaAuditoriaRepository;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaResenaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoResena;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

@Service
public class AuditoriaResenaService implements AuditoriaResenaCommand {

    private final BandejaSalidaAuditoriaRepository auditoriaRepository;

    public AuditoriaResenaService(BandejaSalidaAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarEvento(RegistrarEventoResena solicitud) {
        validarSolicitud(solicitud);

        BandejaSalidaAuditoria evento = new BandejaSalidaAuditoria();
        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(solicitud.idUsuario());
        evento.setFechaEvento(solicitud.fechaEvento());
        evento.setAccion(solicitud.accion());
        evento.setResultado(solicitud.resultado());
        evento.setEntidadAfectada("RESENA");
        evento.setIdEntidadAfectada(solicitud.idResena());
        evento.setDetalle(solicitud.detalle());
        evento.setIdCorrelacion(UUID.randomUUID().toString());
        evento.setEstadoEnvio("PENDIENTE");
        evento.setIntentosEnvio(0);
        evento.setProximaEjecucion(solicitud.fechaEvento());
        evento.setFechaEnvio(null);
        evento.setUltimoError(null);
        auditoriaRepository.save(evento);
    }

    private void validarSolicitud(RegistrarEventoResena solicitud) {
        if (solicitud == null
                || solicitud.idUsuario() == null
                || solicitud.idResena() == null
                || solicitud.fechaEvento() == null
                || solicitud.accion() == null || solicitud.accion().isBlank()
                || solicitud.resultado() == null || solicitud.resultado().isBlank()
                || solicitud.detalle() == null || solicitud.detalle().isBlank()) {
            throw new IllegalArgumentException("Los datos del evento de auditoría de reseña son obligatorios.");
        }
    }
}
