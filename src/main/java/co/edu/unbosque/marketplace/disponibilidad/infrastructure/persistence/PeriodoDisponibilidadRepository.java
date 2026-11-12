package co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;

@Repository
public interface PeriodoDisponibilidadRepository
        extends JpaRepository<PeriodoDisponibilidad, UUID> {

    List<PeriodoDisponibilidad> findByIdAlojamientoOrderByFechaInicioAsc(
            UUID idAlojamiento
    );

    Optional<PeriodoDisponibilidad> findByIdPeriodoAndIdAlojamiento(
            UUID idPeriodo,
            UUID idAlojamiento
    );

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM PeriodoDisponibilidad p
            WHERE p.idAlojamiento = :idAlojamiento
            AND p.habilitado = true
            AND p.fechaInicio < :fechaFin
            AND p.fechaFin > :fechaInicio
            """)
    boolean existeSolapamientoHabilitado(
            @Param("idAlojamiento") UUID idAlojamiento,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );

    @Query("""
            SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END
            FROM PeriodoDisponibilidad p
            WHERE p.idAlojamiento = :idAlojamiento
            AND p.idPeriodo <> :idPeriodo
            AND p.habilitado = true
            AND p.fechaInicio < :fechaFin
            AND p.fechaFin > :fechaInicio
            """)
    boolean existeSolapamientoExcluyendoPeriodo(
            @Param("idAlojamiento") UUID idAlojamiento,
            @Param("idPeriodo") UUID idPeriodo,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}
