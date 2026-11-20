package co.edu.unbosque.marketplace.reporte.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.notificacion.domain.Notificacion;
import co.edu.unbosque.marketplace.notificacion.infrastructure.persistence.NotificacionRepository;
import co.edu.unbosque.marketplace.reporte.infrastructure.persistence.ExportacionReporteInfo;
import co.edu.unbosque.marketplace.reporte.infrastructure.persistence.ExportacionReporteJdbcRepository;

@Component
@Profile("worker")
public class ExportacionReporteWorker {

    private final ExportacionReporteJdbcRepository exportaciones;
    private final ReporteArchivoGenerator generador;
    private final ExportacionReporteService servicio;
    private final NotificacionRepository notificaciones;

    public ExportacionReporteWorker(
            ExportacionReporteJdbcRepository exportaciones,
            ReporteArchivoGenerator generador,
            ExportacionReporteService servicio,
            NotificacionRepository notificaciones) {
        this.exportaciones = exportaciones;
        this.generador = generador;
        this.servicio = servicio;
        this.notificaciones = notificaciones;
    }

    public void procesar(UUID idExportacion) {
        UUID idAdministrador = exportaciones
                .buscarAdministrador(idExportacion)
                .orElse(null);

        if (idAdministrador == null || !exportaciones.iniciar(idExportacion)) {
            return;
        }

        try {
            ExportacionReporteInfo solicitud = exportaciones
                    .buscar(idExportacion, idAdministrador)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "No se encontró la exportación solicitada."));

            ReporteArchivoGenerado resultado =
                    generador.generar(idExportacion, solicitud.formato());

            OffsetDateTime fechaGeneracion = OffsetDateTime.now();

            servicio.completar(
                    idExportacion,
                    idAdministrador,
                    resultado.fechaCorte(),
                    fechaGeneracion,
                    resultado.ruta().toString());

            notificar(
                    idAdministrador,
                    idExportacion,
                    fechaGeneracion,
                    "El reporte está listo para descargar.");

        } catch (Exception error) {
            OffsetDateTime fechaFallo = OffsetDateTime.now();

            servicio.fallar(
                    idExportacion,
                    idAdministrador,
                    fechaFallo,
                    error.getMessage());

            notificar(
                    idAdministrador,
                    idExportacion,
                    fechaFallo,
                    "No fue posible generar el reporte solicitado.");
        }
    }

    private void notificar(
            UUID idAdministrador,
            UUID idExportacion,
            OffsetDateTime fecha,
            String mensaje) {

        Notificacion notificacion = new Notificacion();
        notificacion.setIdNotificacion(UUID.randomUUID());
        notificacion.setIdUsuario(idAdministrador);
        notificacion.setIdReserva(null);
        notificacion.setIdPublicacion(null);
        notificacion.setTipo("EXPORTACION_REPORTE");
        notificacion.setMensaje(mensaje + " Identificador: " + idExportacion);
        notificacion.setFechaCreacion(fecha);
        notificacion.setFechaLectura(null);

        notificaciones.save(notificacion);
    }
}