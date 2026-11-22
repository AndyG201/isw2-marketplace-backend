package co.edu.unbosque.marketplace.reporte.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import co.edu.unbosque.marketplace.reporte.domain.ResumenFinanciero;

/**
 * Lo que ha generado un anfitrión: totales y desglose por publicación. Todo está descontado de devoluciones por
 * cancelaciones y de la comisión de la plataforma.
 */
public record IngresosAnfitrionResponse(
        ResumenFinanciero resumen,
        List<IngresoPublicacion> publicaciones) {

    public IngresosAnfitrionResponse {
        publicaciones = List.copyOf(publicaciones);
    }

    public record IngresoPublicacion(
            UUID idPublicacion,
            String titulo,
            long reservasConPago,
            BigDecimal pagadoPorHuespedes,
            BigDecimal devoluciones,
            BigDecimal comisionPlataforma,
            BigDecimal ganancias) {
    }
}
