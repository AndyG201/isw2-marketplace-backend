package co.edu.unbosque.marketplace.reporte.application.port;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardIndicadoresRepository {

    IndicadoresUsuarios consultarIndicadoresUsuarios();

    long contarPublicacionesActivas();

    Map<String, Long> contarReservasPorEstado();

    BigDecimal calcularIngresosEstimados();

    List<CiudadDemanda> consultarCiudadesConMayorDemanda();

    record IndicadoresUsuarios(
            long total,
            long activos,
            long huespedes,
            long anfitriones) {
    }

    record CiudadDemanda(
            String ciudad,
            long solicitudesCotizacion) {
    }
}
