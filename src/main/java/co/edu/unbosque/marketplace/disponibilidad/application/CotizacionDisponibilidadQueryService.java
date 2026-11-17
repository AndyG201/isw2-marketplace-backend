package co.edu.unbosque.marketplace.disponibilidad.application;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.NocheReservadaRepository;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.PeriodoDisponibilidadRepository;

import co.edu.unbosque.marketplace.cotizacion.contracts.CotizacionDisponibilidadQuery;

@Service
public class CotizacionDisponibilidadQueryService
        implements CotizacionDisponibilidadQuery {

    private final PeriodoDisponibilidadRepository periodoRepository;
    private final NocheReservadaRepository nocheRepository;

    public CotizacionDisponibilidadQueryService(
            PeriodoDisponibilidadRepository periodoRepository,
            NocheReservadaRepository nocheRepository) {
        this.periodoRepository = periodoRepository;
        this.nocheRepository = nocheRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean estaDisponible(
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

        if (!periodosCubrenFechas(periodos, fechaEntrada, fechaSalida)) {
            return false;
        }

        return !nocheRepository.existeNocheOcupadaEnRango(
                idAlojamiento,
                fechaEntrada,
                fechaSalida
        );
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