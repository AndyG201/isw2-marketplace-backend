package co.edu.unbosque.marketplace.reporte.application;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;
import co.edu.unbosque.marketplace.reporte.application.dto.IngresosAnfitrionResponse;
import co.edu.unbosque.marketplace.reporte.application.dto.IngresosAnfitrionResponse.IngresoPublicacion;
import co.edu.unbosque.marketplace.reporte.application.port.RepartoReservasRepository;
import co.edu.unbosque.marketplace.reporte.domain.CalculadoraReparto;
import co.edu.unbosque.marketplace.reporte.domain.RepartoReserva;
import co.edu.unbosque.marketplace.reporte.domain.ResumenFinanciero;

@Service
public class IngresosAnfitrionService {

    private final RepartoReservasRepository repartos;

    public IngresosAnfitrionService(RepartoReservasRepository repartos) {
        this.repartos = repartos;
    }

    @Transactional(readOnly = true)
    public IngresosAnfitrionResponse consultar(UUID idAnfitrion) {
        if (idAnfitrion == null) {
            throw new SesionRequeridaException("Debes iniciar sesión para consultar tus ingresos.");
        }

        List<RepartoReserva> deLasReservas = repartos.pagosAprobadosDelAnfitrion(idAnfitrion).stream()
                .map(CalculadoraReparto::repartir)
                .toList();

        Map<UUID, List<RepartoReserva>> porPublicacion = new LinkedHashMap<>();
        for (RepartoReserva reparto : deLasReservas) {
            porPublicacion.computeIfAbsent(reparto.idPublicacion(), id -> new java.util.ArrayList<>()).add(reparto);
        }

        List<IngresoPublicacion> publicaciones = porPublicacion.values().stream()
                .map(IngresosAnfitrionService::resumirPublicacion)
                .sorted(Comparator.comparing(IngresoPublicacion::ganancias).reversed()
                        .thenComparing(IngresoPublicacion::titulo))
                .toList();

        return new IngresosAnfitrionResponse(ResumenFinanciero.de(deLasReservas), publicaciones);
    }

    private static IngresoPublicacion resumirPublicacion(List<RepartoReserva> repartos) {
        ResumenFinanciero resumen = ResumenFinanciero.de(repartos);
        RepartoReserva primero = repartos.get(0);
        BigDecimal ganancias = resumen.gananciasAnfitriones();
        return new IngresoPublicacion(primero.idPublicacion(), primero.tituloPublicacion(), resumen.reservasConPago(),
                resumen.pagadoPorHuespedes(), resumen.devoluciones(), resumen.comisionPlataforma(), ganancias);
    }
}
