package co.edu.unbosque.marketplace.disponibilidad.application.port;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;

public interface PeriodoDisponibilidadRepository {

    <S extends PeriodoDisponibilidad> S save(S entidad);
    <S extends PeriodoDisponibilidad> List<S> saveAll(Iterable<S> entidades);
    <S extends PeriodoDisponibilidad> S saveAndFlush(S entidad);
    Optional<PeriodoDisponibilidad> findById(UUID id);
    List<PeriodoDisponibilidad> findAll();
    boolean existsById(UUID id);
    long count();
    void delete(PeriodoDisponibilidad entidad);
    void deleteById(UUID id);
    void flush();
    List<PeriodoDisponibilidad> findByIdAlojamientoOrderByFechaInicioAsc(UUID idAlojamiento);
    Optional<PeriodoDisponibilidad> findByIdPeriodoAndIdAlojamiento(UUID idPeriodo, UUID idAlojamiento);
    boolean existeSolapamientoHabilitado(UUID idAlojamiento, LocalDate fechaInicio, LocalDate fechaFin);
    boolean existeSolapamientoExcluyendoPeriodo(UUID idAlojamiento, UUID idPeriodo, LocalDate fechaInicio, LocalDate fechaFin);
}
