package co.edu.unbosque.marketplace.auditoria.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.application.port.BandejaSalidaAuditoriaRepository;
import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaReservaCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoReserva;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;

@Service
public class AuditoriaReservaService implements AuditoriaReservaCommand {
    private final BandejaSalidaAuditoriaRepository repository;

    public AuditoriaReservaService(BandejaSalidaAuditoriaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarEvento(RegistrarEventoReserva solicitud) {
        if (solicitud == null || solicitud.idReserva() == null || solicitud.fechaEvento() == null
                || solicitud.accion() == null || solicitud.accion().isBlank()
                || solicitud.resultado() == null || solicitud.resultado().isBlank()
                || solicitud.detalle() == null || solicitud.detalle().isBlank()) {
            throw new IllegalArgumentException("Los datos de auditoría de reserva son obligatorios.");
        }
        BandejaSalidaAuditoria evento = new BandejaSalidaAuditoria();
        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(solicitud.idUsuario());
        evento.setFechaEvento(solicitud.fechaEvento());
        evento.setAccion(solicitud.accion());
        evento.setResultado(solicitud.resultado());
        evento.setEntidadAfectada("RESERVA");
        evento.setIdEntidadAfectada(solicitud.idReserva());
        evento.setDetalle(solicitud.detalle());
        evento.setIdCorrelacion(UUID.randomUUID().toString());
        evento.setEstadoEnvio("PENDIENTE");
        evento.setIntentosEnvio(0);
        evento.setProximaEjecucion(solicitud.fechaEvento());
        evento.setFechaEnvio(null);
        evento.setUltimoError(null);
        repository.save(evento);
    }
}
