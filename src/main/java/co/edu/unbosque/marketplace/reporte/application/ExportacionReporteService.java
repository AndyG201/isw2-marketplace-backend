package co.edu.unbosque.marketplace.reporte.application;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaAdministracionCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAdministracion;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.compartido.domain.PlazoVencidoException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.reporte.api.response.ExportacionReporteResponse;
import co.edu.unbosque.marketplace.reporte.infrastructure.persistence.ExportacionReporteInfo;
import co.edu.unbosque.marketplace.reporte.infrastructure.persistence.ExportacionReporteJdbcRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajosRedis;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

@Service
public class ExportacionReporteService {

    private final ExportacionReporteJdbcRepository repositorio;
    private final AuditoriaAdministracionCommand auditoria;
    private final ColaTrabajosRedis cola;
    private final ObjectMapper mapper;

    public ExportacionReporteService(
            ExportacionReporteJdbcRepository repositorio,
            AuditoriaAdministracionCommand auditoria,
            ColaTrabajosRedis cola,
            ObjectMapper mapper) {
        this.repositorio = repositorio;
        this.auditoria = auditoria;
        this.cola = cola;
        this.mapper = mapper;
    }

    @Transactional
    public ExportacionReporteResponse solicitar(
            UUID idAdministrador,
            String formatoEntrada) {

        if (idAdministrador == null || formatoEntrada == null) {
            throw new IllegalArgumentException(
                    "El administrador y el formato son obligatorios.");
        }

        String formato = formatoEntrada.trim().toUpperCase();
        if (!formato.equals("PDF") && !formato.equals("EXCEL")) {
            throw new IllegalArgumentException(
                    "El formato debe ser PDF o EXCEL.");
        }

        UUID idExportacion = UUID.randomUUID();
        OffsetDateTime fechaSolicitud = OffsetDateTime.now();

        repositorio.crear(
                idExportacion,
                idAdministrador,
                formato,
                fechaSolicitud);

        registrarAuditoria(
                idAdministrador,
                idExportacion,
                "EXPORTACION_SOLICITADA",
                "EXITOSO",
                fechaSolicitud,
                Map.of(
                        "formato", formato,
                        "tipoReporte", "DASHBOARD_GESTION"));

        // El puntaje de prioridad se define en TipoTrabajo.
        cola.encolar(TipoTrabajo.EXPORTAR_REPORTE, idExportacion.toString());

        return new ExportacionReporteResponse(
                idExportacion,
                "SOLICITADO",
                formato,
                fechaSolicitud,
                "La exportación quedó en cola.",
                null);
    }

    @Transactional(readOnly = true)
    public ExportacionReporteResponse consultar(
            UUID idExportacion,
            UUID idAdministrador) {

        ExportacionReporteInfo info =
                obtenerExportacion(idExportacion, idAdministrador);

        String urlDescarga = "COMPLETADO".equals(info.estado())
                ? "/administracion/reportes/exportaciones/"
                        + idExportacion + "/archivo"
                : null;

        return new ExportacionReporteResponse(
                info.idExportacion(),
                info.estado(),
                info.formato(),
                info.fechaSolicitud(),
                mensajeEstado(info.estado()),
                urlDescarga);
    }

    @Transactional(readOnly = true)
    public ArchivoExportado descargar(
            UUID idExportacion,
            UUID idAdministrador) {

        ExportacionReporteInfo info =
                obtenerExportacion(idExportacion, idAdministrador);

        if (!"COMPLETADO".equals(info.estado())) {
            throw new ConflictoNegocioException(
                    "El archivo todavía no está disponible.");
        }

        if (info.rutaArchivo() == null || info.rutaArchivo().isBlank()) {
            throw new RecursoNoEncontradoException(
                    "La exportación no tiene un archivo asociado.");
        }

        try {
            Path archivo = Path.of(info.rutaArchivo())
                    .toAbsolutePath()
                    .normalize();

            if (!Files.isRegularFile(archivo)) {
                throw new PlazoVencidoException(
                        "El archivo exportado ya no está disponible.");
            }

            boolean esPdf = "PDF".equals(info.formato());
            String extension = esPdf ? "pdf" : "xlsx";
            String tipo = esPdf
                    ? "application/pdf"
                    : "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

            return new ArchivoExportado(
                    "reporte-" + idExportacion + "." + extension,
                    tipo,
                    Files.readAllBytes(archivo));

        } catch (java.io.IOException e) {
            throw new FallaInternaException(
                    "No fue posible leer el archivo exportado.",
                    e);
        }
    }

    @Transactional
    public void completar(
            UUID idExportacion,
            UUID idAdministrador,
            OffsetDateTime fechaCorte,
            OffsetDateTime fechaGeneracion,
            String rutaArchivo) {

        repositorio.completar(
                idExportacion,
                fechaCorte,
                fechaGeneracion,
                rutaArchivo);

        ExportacionReporteInfo info =
                obtenerExportacion(idExportacion, idAdministrador);

        registrarAuditoria(
                idAdministrador,
                idExportacion,
                "EXPORTACION_COMPLETADA",
                "EXITOSO",
                fechaGeneracion,
                Map.of(
                        "formato", info.formato(),
                        "fechaCorte", fechaCorte.toString(),
                        "rutaArchivo", rutaArchivo));
    }

    @Transactional
    public void fallar(
            UUID idExportacion,
            UUID idAdministrador,
            OffsetDateTime fecha,
            String detalleError) {

        repositorio.fallar(idExportacion);

        String detalle = detalleError == null || detalleError.isBlank()
                ? "Error no especificado."
                : detalleError.substring(0, Math.min(detalleError.length(), 500));

        registrarAuditoria(
                idAdministrador,
                idExportacion,
                "EXPORTACION_FALLIDA",
                "FALLIDO",
                fecha,
                Map.of("detalle", detalle));
    }

    private ExportacionReporteInfo obtenerExportacion(
            UUID idExportacion,
            UUID idAdministrador) {

        return repositorio.buscar(idExportacion, idAdministrador)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "La exportación no existe."));
    }

    private void registrarAuditoria(
            UUID idUsuario,
            UUID idExportacion,
            String accion,
            String resultado,
            OffsetDateTime fecha,
            Map<String, Object> datos) {

        auditoria.registrarEvento(new RegistrarEventoAdministracion(
                idUsuario,
                idExportacion,
                "EXPORTACION_REPORTE",
                fecha,
                accion,
                resultado,
                serializar(datos),
                idExportacion.toString()));
    }
    private String serializar(Map<String, Object> datos) {
        try {
            return mapper.writeValueAsString(datos);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "No fue posible serializar el evento de auditoría.",
                    e);
        }
    }

    private String mensajeEstado(String estado) {
        return switch (estado) {
            case "SOLICITADO" -> "La exportación está en cola.";
            case "EN_PROCESO" -> "El worker está generando el archivo.";
            case "COMPLETADO" -> "El archivo está listo para descargar.";
            case "FALLIDO" -> "No se pudo generar el archivo.";
            default -> "Estado desconocido.";
        };
    }
}