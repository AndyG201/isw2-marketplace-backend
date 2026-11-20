package co.edu.unbosque.marketplace.auditoria.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaAdministracionCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAdministracion;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.BandejaSalidaAuditoriaRepository;

@Service
public class AuditoriaAdministracionService
        implements AuditoriaAdministracionCommand {

    private final BandejaSalidaAuditoriaRepository auditoriaRepository;

    public AuditoriaAdministracionService(
            BandejaSalidaAuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarEvento(RegistrarEventoAdministracion solicitud) {
        if (solicitud == null
                || solicitud.fechaEvento() == null
                || solicitud.entidadAfectada() == null
                || solicitud.entidadAfectada().isBlank()
                || solicitud.accion() == null
                || solicitud.accion().isBlank()
                || solicitud.resultado() == null
                || solicitud.resultado().isBlank()) {
            throw new IllegalArgumentException(
                    "Los datos del evento de auditoría son obligatorios.");
        }

        BandejaSalidaAuditoria evento = new BandejaSalidaAuditoria();
        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(solicitud.idUsuario());
        evento.setFechaEvento(solicitud.fechaEvento());
        evento.setAccion(solicitud.accion());
        evento.setResultado(solicitud.resultado());
        evento.setEntidadAfectada(solicitud.entidadAfectada());
        evento.setIdEntidadAfectada(solicitud.idEntidadAfectada());
        evento.setDetalle(solicitud.detalle());
        evento.setIdCorrelacion(
                solicitud.idCorrelacion() == null
                        ? UUID.randomUUID().toString()
                        : solicitud.idCorrelacion());
        evento.setEstadoEnvio("PENDIENTE");
        evento.setIntentosEnvio(0);
        evento.setProximaEjecucion(solicitud.fechaEvento());
        evento.setFechaEnvio(null);
        evento.setUltimoError(null);

        auditoriaRepository.save(evento);
    }
}