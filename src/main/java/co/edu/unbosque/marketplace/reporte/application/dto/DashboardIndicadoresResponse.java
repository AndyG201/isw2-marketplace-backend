package co.edu.unbosque.marketplace.reporte.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record DashboardIndicadoresResponse(
        IndicadoresUsuarios usuarios,
        long publicacionesActivas,
        Map<String, Long> reservasPorEstado,
        BigDecimal ingresosEstimados,
        List<DemandaCiudad> ciudadesConMayorDemanda,
        OffsetDateTime fechaCorteDatos) {

    public DashboardIndicadoresResponse {
        reservasPorEstado = Collections.unmodifiableMap(new LinkedHashMap<>(reservasPorEstado));
        ciudadesConMayorDemanda = List.copyOf(ciudadesConMayorDemanda);
    }

    public record IndicadoresUsuarios(
            long total,
            long activos,
            long huespedes,
            long anfitriones) {
    }

    public record DemandaCiudad(
            String ciudad,
            long solicitudesCotizacion) {
    }
}
