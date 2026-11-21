package co.edu.unbosque.marketplace.auditoria.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.BandejaSalidaAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.BandejaSalidaAuditoriaRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Service
public class AuditoriaOutboxService implements AuditoriaEventoCommand {

    private static final int MAX_ERROR = 1000;
    private static final long MAX_REINTENTO_SEGUNDOS = 3600;

    private final BandejaSalidaAuditoriaRepository repository;
    private final ColaTrabajosRedis cola;

    public AuditoriaOutboxService(
            BandejaSalidaAuditoriaRepository repository,
            ColaTrabajosRedis cola) {
        this.repository = repository;
        this.cola = cola;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarEvento(RegistrarEventoAuditoria solicitud) {
        guardar(solicitud);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEventoIndependiente(RegistrarEventoAuditoria solicitud) {
        guardar(solicitud);
    }

    @Transactional
    public int encolarPendientes() {
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        List<BandejaSalidaAuditoria> pendientes = repository
                .findTop100ByEstadoEnvioInAndProximaEjecucionLessThanEqualOrderByProximaEjecucionAsc(
                        List.of("PENDIENTE", "ERROR", "EN_PROCESO"), ahora);

        if (pendientes.isEmpty()) {
            return 0;
        }

        OffsetDateTime limiteArrendamiento = ahora.plusSeconds(60);
        pendientes.forEach(evento -> {
            evento.setEstadoEnvio("EN_PROCESO");
            evento.setIntentosEnvio(evento.getIntentosEnvio() + 1);
            evento.setProximaEjecucion(limiteArrendamiento);
        });
        repository.saveAll(pendientes);

        pendientes.forEach(evento -> cola.encolar(
                TipoTrabajo.COPIAR_AUDITORIA,
                evento.getIdEvento().toString()));
        return pendientes.size();
    }

    @Transactional(readOnly = true)
    public BandejaSalidaAuditoria buscarParaProcesar(UUID idEvento) {
        return repository.findById(idEvento).orElse(null);
    }

    @Transactional
    public void marcarEnviado(UUID idEvento) {
        repository.findById(idEvento).ifPresent(evento -> {
            if (!"ENVIADO".equals(evento.getEstadoEnvio())) {
                OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
                evento.setEstadoEnvio("ENVIADO");
                evento.setFechaEnvio(ahora);
                evento.setProximaEjecucion(ahora);
                evento.setUltimoError(null);
                repository.save(evento);
            }
        });
    }

    @Transactional
    public void marcarError(UUID idEvento, Exception error) {
        repository.findById(idEvento).ifPresent(evento -> {
            if ("ENVIADO".equals(evento.getEstadoEnvio())) {
                return;
            }
            OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
            evento.setEstadoEnvio("ERROR");
            evento.setProximaEjecucion(ahora.plusSeconds(segundosReintento(
                    evento.getIntentosEnvio())));
            String mensaje = error == null
                    ? "Error desconocido al archivar el evento."
                    : error.getClass().getSimpleName() + ": " + error.getMessage();
            evento.setUltimoError(mensaje.length() > MAX_ERROR
                    ? mensaje.substring(0, MAX_ERROR)
                    : mensaje);
            repository.save(evento);
        });
    }

    private long segundosReintento(int intentos) {
        int exponente = Math.min(Math.max(intentos - 1, 0), 7);
        return Math.min(MAX_REINTENTO_SEGUNDOS, 30L * (1L << exponente));
    }

    private void guardar(RegistrarEventoAuditoria solicitud) {
        validar(solicitud);
        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        BandejaSalidaAuditoria evento = new BandejaSalidaAuditoria();
        evento.setIdEvento(UUID.randomUUID());
        evento.setIdUsuario(solicitud.idUsuario());
        evento.setFechaEvento(solicitud.fechaEvento());
        evento.setAccion(solicitud.accion().trim());
        evento.setResultado(solicitud.resultado().trim());
        evento.setEntidadAfectada(solicitud.entidadAfectada().trim());
        evento.setIdEntidadAfectada(solicitud.idEntidadAfectada());
        evento.setDetalle(solicitud.detalle());
        evento.setIdCorrelacion(solicitud.idCorrelacion() == null
                || solicitud.idCorrelacion().isBlank()
                        ? UUID.randomUUID().toString()
                        : solicitud.idCorrelacion().trim());
        evento.setEstadoEnvio("PENDIENTE");
        evento.setIntentosEnvio(0);
        evento.setProximaEjecucion(ahora);
        evento.setFechaEnvio(null);
        evento.setUltimoError(null);
        repository.save(evento);
    }

    private void validar(RegistrarEventoAuditoria solicitud) {
        if (solicitud == null || solicitud.fechaEvento() == null
                || solicitud.accion() == null || solicitud.accion().isBlank()
                || solicitud.accion().trim().length() > 100
                || solicitud.resultado() == null || solicitud.resultado().isBlank()
                || solicitud.resultado().trim().length() > 30
                || solicitud.entidadAfectada() == null
                || solicitud.entidadAfectada().isBlank()
                || solicitud.entidadAfectada().trim().length() > 50
                || (solicitud.idCorrelacion() != null
                        && solicitud.idCorrelacion().length() > 100)) {
            throw new IllegalArgumentException(
                    "Los datos del evento de auditoría son obligatorios o superan su longitud máxima.");
        }
    }
}