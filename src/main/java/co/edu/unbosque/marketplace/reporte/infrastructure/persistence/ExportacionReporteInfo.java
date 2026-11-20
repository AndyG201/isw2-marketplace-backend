package co.edu.unbosque.marketplace.reporte.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExportacionReporteInfo(
        UUID idExportacion,
        UUID idAdministrador,
        String formato,
        String estado,
        OffsetDateTime fechaSolicitud,
        OffsetDateTime fechaCorteDatos,
        OffsetDateTime fechaGeneracion,
        String rutaArchivo) {
}