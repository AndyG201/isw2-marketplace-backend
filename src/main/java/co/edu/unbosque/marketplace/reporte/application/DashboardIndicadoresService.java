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
import co.edu.unbosque.marketplace.reporte.application.port.RepartoReservasRepository;
import co.edu.unbosque.marketplace.reporte.domain.CalculadoraReparto;
import co.edu.unbosque.marketplace.reporte.domain.ResumenFinanciero;

@Service
public class DashboardIndicadoresService {

    private final DashboardIndicadoresRepository indicadores;
    private final RepartoReservasRepository repartos;

    public DashboardIndicadoresService(
            DashboardIndicadoresRepository indicadores,
            RepartoReservasRepository repartos) {
        this.indicadores = indicadores;
        this.repartos = repartos;
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
                finanzas(),
                ciudades,
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    /** Pagado, devuelto y repartido entre la plataforma y los anfitriones, según las reservas pagadas. */
    private ResumenFinanciero finanzas() {
        return ResumenFinanciero.de(repartos.pagosAprobados().stream().map(CalculadoraReparto::repartir).toList());
    }
}
