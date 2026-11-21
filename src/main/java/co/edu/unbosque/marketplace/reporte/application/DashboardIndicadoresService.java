package co.edu.unbosque.marketplace.reporte.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.reporte.application.dto.DashboardIndicadoresResponse;
import co.edu.unbosque.marketplace.reporte.application.port.DashboardIndicadoresRepository;

@Service
public class DashboardIndicadoresService {

    private final DashboardIndicadoresRepository indicadores;

    public DashboardIndicadoresService(DashboardIndicadoresRepository indicadores) {
        this.indicadores = indicadores;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DashboardIndicadoresResponse consultar() {
        var usuariosDatos = indicadores.consultarIndicadoresUsuarios();
        var usuarios = new DashboardIndicadoresResponse.IndicadoresUsuarios(
                usuariosDatos.total(),
                usuariosDatos.activos(),
                usuariosDatos.huespedes(),
                usuariosDatos.anfitriones());

        Map<String, Long> reservasPorEstado = indicadores.contarReservasPorEstado();

        List<DashboardIndicadoresResponse.DemandaCiudad> ciudades =
                indicadores.consultarCiudadesConMayorDemanda().stream()
                        .map(ciudad -> new DashboardIndicadoresResponse.DemandaCiudad(
                                ciudad.ciudad(),
                                ciudad.solicitudesCotizacion()))
                        .toList();

        return new DashboardIndicadoresResponse(
                usuarios,
                indicadores.contarPublicacionesActivas(),
                reservasPorEstado,
                indicadores.calcularIngresosEstimados(),
                ciudades,
                OffsetDateTime.now(ZoneOffset.UTC));
    }
}
