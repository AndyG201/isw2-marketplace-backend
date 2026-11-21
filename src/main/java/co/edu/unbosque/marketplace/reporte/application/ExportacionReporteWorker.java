package co.edu.unbosque.marketplace.reporte.application;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.reporte.application.port.ExportacionReporteInfo;
import co.edu.unbosque.marketplace.reporte.application.port.ExportacionReporteRepository;
import co.edu.unbosque.marketplace.notificacion.contracts.CrearNotificacionUsuario;
import co.edu.unbosque.marketplace.notificacion.contracts.NotificacionUsuarioCommand;
import co.edu.unbosque.marketplace.trabajo.ProcesadorTrabajo;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Component
@Profile("worker")
public class ExportacionReporteWorker implements ProcesadorTrabajo {

    private final ExportacionReporteRepository exportaciones;
    private final ReporteArchivoGenerator generador;
    private final ExportacionReporteService servicio;
    private final NotificacionUsuarioCommand notificaciones;

    public ExportacionReporteWorker(
            ExportacionReporteRepository exportaciones,
            ReporteArchivoGenerator generador,
            ExportacionReporteService servicio,
            NotificacionUsuarioCommand notificaciones) {
        this.exportaciones = exportaciones;
        this.generador = generador;
        this.servicio = servicio;
        this.notificaciones = notificaciones;
    }

    @Override
    public boolean soporta(TipoTrabajo tipo) {
        return tipo == TipoTrabajo.EXPORTAR_REPORTE;
    }

    @Override
    public void procesar(TipoTrabajo tipo, String identificador) {
        if (!soporta(tipo)) {
            throw new IllegalArgumentException(
                    "El procesador de reportes no admite el trabajo " + tipo);
        }
        procesar(UUID.fromString(identificador));
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

        notificaciones.crearNotificacion(new CrearNotificacionUsuario(
                idAdministrador,
                "EXPORTACION_REPORTE",
                mensaje + " Identificador: " + idExportacion,
                fecha));
    }
}
