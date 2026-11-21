package co.edu.unbosque.marketplace.reporte.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ExportacionReporteResponse(
        UUID idExportacion,
        String estado,
        String formato,
        OffsetDateTime fechaSolicitud,
        String mensaje,
        String urlDescarga) {
}