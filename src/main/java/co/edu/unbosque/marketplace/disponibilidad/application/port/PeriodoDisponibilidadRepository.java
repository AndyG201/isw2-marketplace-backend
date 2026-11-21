package co.edu.unbosque.marketplace.disponibilidad.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.compartido.domain.RepositorioBasico;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;

public interface PeriodoDisponibilidadRepository extends RepositorioBasico<PeriodoDisponibilidad, UUID> {
    List<PeriodoDisponibilidad> findByIdAlojamientoOrderByFechaInicioAsc(UUID idAlojamiento);
    Optional<PeriodoDisponibilidad> findByIdPeriodoAndIdAlojamiento(UUID idPeriodo, UUID idAlojamiento);
    boolean existeSolapamientoHabilitado(UUID idAlojamiento, LocalDate fechaInicio, LocalDate fechaFin);
    boolean existeSolapamientoExcluyendoPeriodo(UUID idAlojamiento, UUID idPeriodo, LocalDate fechaInicio, LocalDate fechaFin);
}
