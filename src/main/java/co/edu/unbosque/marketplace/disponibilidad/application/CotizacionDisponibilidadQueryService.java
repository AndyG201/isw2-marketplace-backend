package co.edu.unbosque.marketplace.disponibilidad.application;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.disponibilidad.application.port.PeriodoDisponibilidadRepository;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.reserva.contracts.NochesOcupadasQuery;
import co.edu.unbosque.marketplace.reserva.contracts.PeriodosHabilitadosQuery;

@Service
public class CotizacionDisponibilidadQueryService
        implements CotizacionDisponibilidadQuery, PeriodosHabilitadosQuery {

    private final PeriodoDisponibilidadRepository periodoRepository;
    private final NochesOcupadasQuery nochesOcupadas;

    public CotizacionDisponibilidadQueryService(
            PeriodoDisponibilidadRepository periodoRepository,
            NochesOcupadasQuery nochesOcupadas) {
        this.periodoRepository = periodoRepository;
        this.nochesOcupadas = nochesOcupadas;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(
            UUID idAlojamiento,
            LocalDate fechaEntrada,
            LocalDate fechaSalida) {

        return rangoHabilitado(idAlojamiento, fechaEntrada, fechaSalida)
                && !nochesOcupadas.hayNochesOcupadas(idAlojamiento, fechaEntrada, fechaSalida);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean rangoHabilitado(
            UUID idAlojamiento,
            LocalDate fechaEntrada,
            LocalDate fechaSalida) {

        if (idAlojamiento == null
                || fechaEntrada == null
                || fechaSalida == null
                || !fechaSalida.isAfter(fechaEntrada)) {
            return false;
        }

        List<PeriodoDisponibilidad> periodos =
                periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                        idAlojamiento
                );

        return periodosCubrenFechas(periodos, fechaEntrada, fechaSalida);
    }

    private boolean periodosCubrenFechas(
            List<PeriodoDisponibilidad> periodos,
            LocalDate fechaEntrada,
            LocalDate fechaSalida) {

        LocalDate cubiertoHasta = fechaEntrada;

        for (PeriodoDisponibilidad periodo : periodos) {
            if (!periodo.isHabilitado()) {
                continue;
            }

            if (periodo.getFechaFin().isBefore(cubiertoHasta)) {
                continue;
            }

            if (periodo.getFechaInicio().isAfter(cubiertoHasta)) {
                break;
            }

            if (periodo.getFechaFin().isAfter(cubiertoHasta)) {
                cubiertoHasta = periodo.getFechaFin();
            }

            if (!cubiertoHasta.isBefore(fechaSalida)) {
                return true;
            }
        }

        return false;
    }
}