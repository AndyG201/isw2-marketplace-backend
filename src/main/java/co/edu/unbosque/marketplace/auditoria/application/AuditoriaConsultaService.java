package co.edu.unbosque.marketplace.auditoria.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.auditoria.api.response.EventoAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.api.response.PaginaAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.domain.AlmacenamientoInmutableAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoriaIndice;
import co.edu.unbosque.marketplace.auditoria.domain.FiltrosAuditoria;
import co.edu.unbosque.marketplace.auditoria.infrastructure.persistence.AuditoriaConsultaJdbcRepository;

@Service
public class AuditoriaConsultaService {

    private static final int TAMANO_MAXIMO = 100;

    private final AuditoriaConsultaJdbcRepository repository;
    private final ObjectProvider<AlmacenamientoInmutableAuditoria> backblazeProvider;

    public AuditoriaConsultaService(
            AuditoriaConsultaJdbcRepository repository,
            ObjectProvider<AlmacenamientoInmutableAuditoria> backblazeProvider) {

        this.repository = repository;
        this.backblazeProvider = backblazeProvider;
    }

    public PaginaAuditoriaResponse consultar(
            UUID idUsuario,
            OffsetDateTime desde,
            OffsetDateTime hasta,
            String accion,
            String resultado,
            String entidadAfectada,
            int pagina,
            int tamano) {

        validarConsulta(desde, hasta, pagina, tamano);

        FiltrosAuditoria filtros = new FiltrosAuditoria(
                idUsuario,
                desde,
                hasta,
                normalizar(accion),
                normalizar(resultado),
                normalizar(entidadAfectada)
        );

        try {

            long total = repository.contar(filtros);

            List<EventoAuditoriaIndice> indices = repository.consultar(
                    filtros,
                    tamano,
                    (long) pagina * tamano
            );

            List<EventoAuditoriaResponse> eventos;

            if (indices.isEmpty()) {
                eventos = List.of();
            } else {

                AlmacenamientoInmutableAuditoria almacenamiento =
                        obtenerAlmacenamiento();

                eventos = indices.stream()
                        .map(indice -> almacenamiento.obtenerEvento(
                                indice.claveObjeto(),
                                indice.versionObjeto(),
                                indice.sha256(),
                                indice.idEvento()
                        ))
                        .map(this::convertir)
                        .toList();
            }

            int paginas = total == 0
                    ? 0
                    : (int) ((total + tamano - 1) / tamano);

            return new PaginaAuditoriaResponse(
                    eventos,
                    pagina,
                    tamano,
                    total,
                    paginas,
                    total == 0
                            ? "No hay eventos de auditoría para los filtros indicados."
                            : null
            );

        } catch (DataAccessException | IllegalStateException error) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible consultar el almacén de auditoría.",
                    error
            );
        }
    }

    public EventoAuditoriaResponse consultar(UUID idEvento) {

        if (idEvento == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Debes proporcionar el identificador del evento.");
        }

        try {

            EventoAuditoriaIndice indice = repository.buscar(idEvento)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "El evento de auditoría no existe."));

            AlmacenamientoInmutableAuditoria almacenamiento =
                    obtenerAlmacenamiento();

            EventoAuditoria evento = almacenamiento.obtenerEvento(
                    indice.claveObjeto(),
                    indice.versionObjeto(),
                    indice.sha256(),
                    indice.idEvento()
            );

            return convertir(evento);

        } catch (DataAccessException | IllegalStateException error) {

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible recuperar el evento de auditoría.",
                    error
            );
        }
    }

    private AlmacenamientoInmutableAuditoria obtenerAlmacenamiento() {

        AlmacenamientoInmutableAuditoria servicio =
                backblazeProvider.getIfAvailable();

        if (servicio == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El almacenamiento Backblaze no está habilitado."
            );
        }

        return servicio;
    }

    private void validarConsulta(
            OffsetDateTime desde,
            OffsetDateTime hasta,
            int pagina,
            int tamano) {

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha desde no puede ser posterior a la fecha hasta.");
        }

        if (pagina < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La página debe ser mayor o igual a cero.");
        }

        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tamaño debe estar entre 1 y " + TAMANO_MAXIMO + ".");
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank()
                ? null
                : valor.trim();
    }

    private EventoAuditoriaResponse convertir(EventoAuditoria evento) {

        return new EventoAuditoriaResponse(
                evento.idEvento(),
                evento.idUsuario(),
                evento.fechaEvento(),
                evento.accion(),
                evento.resultado(),
                evento.entidadAfectada(),
                evento.idEntidadAfectada(),
                evento.detalleJson(),
                evento.idCorrelacion()
        );
    }
}
