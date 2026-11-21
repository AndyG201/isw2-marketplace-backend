package co.edu.unbosque.marketplace.disponibilidad.infrastructure.persitence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.PeriodoDisponibilidadRepository;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class PeriodoDisponibilidadRepositoryTest {

    @Autowired
    private PeriodoDisponibilidadRepository repository;

    private UUID idAlojamiento;
    private UUID idOtroAlojamiento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @BeforeEach
    void setUp() {
        idAlojamiento = UUID.randomUUID();
        idOtroAlojamiento = UUID.randomUUID();

        fechaInicio = LocalDate.of(2026, 11, 20);
        fechaFin = LocalDate.of(2026, 11, 30);

        repository.deleteAll();
    }

    private PeriodoDisponibilidad crearPeriodo(
            UUID idAlojamiento,
            LocalDate inicio,
            LocalDate fin,
            boolean habilitado
    ) {
        PeriodoDisponibilidad periodo = new PeriodoDisponibilidad(
                UUID.randomUUID(),
                idAlojamiento,
                inicio,
                fin,
                habilitado,
                OffsetDateTime.now()
        );

        return repository.saveAndFlush(periodo);
    }

    @Test
    void buscarPorAlojamientoOrdenadoPorFechaInicio() {
        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15),
                true
        );

        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 25),
                true
        );

        crearPeriodo(
                idOtroAlojamiento,
                LocalDate.of(2026, 11, 15),
                LocalDate.of(2026, 11, 18),
                true
        );

        List<PeriodoDisponibilidad> resultado =
                repository.findByIdAlojamientoOrderByFechaInicioAsc(
                        idAlojamiento
                );

        assertEquals(2, resultado.size());
        assertTrue(
                resultado.get(0).getFechaInicio()
                        .isBefore(resultado.get(1).getFechaInicio())
        );
    }

    @Test
    void buscarPorAlojamientoSinResultados() {
        List<PeriodoDisponibilidad> resultado =
                repository.findByIdAlojamientoOrderByFechaInicioAsc(
                        idAlojamiento
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void buscarPorIdPeriodoYIdAlojamiento() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                fechaInicio,
                fechaFin,
                true
        );

        Optional<PeriodoDisponibilidad> resultado =
                repository.findByIdPeriodoAndIdAlojamiento(
                        periodo.getIdPeriodo(),
                        idAlojamiento
                );

        assertTrue(resultado.isPresent());
        assertEquals(
                periodo.getIdPeriodo(),
                resultado.get().getIdPeriodo()
        );
    }

    @Test
    void buscarPorIdPeriodoYAlojamientoIncorrecto() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                fechaInicio,
                fechaFin,
                true
        );

        Optional<PeriodoDisponibilidad> resultado =
                repository.findByIdPeriodoAndIdAlojamiento(
                        periodo.getIdPeriodo(),
                        idOtroAlojamiento
                );

        assertFalse(resultado.isPresent());
    }

    @Test
    void existeSolapamientoHabilitado() {
        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado = repository.existeSolapamientoHabilitado(
                idAlojamiento,
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 12, 5)
        );

        assertTrue(resultado);
    }

    @Test
    void noExisteSolapamientoHabilitado() {
        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado = repository.existeSolapamientoHabilitado(
                idAlojamiento,
                LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 10)
        );

        assertFalse(resultado);
    }

    @Test
    void noExisteSolapamientoConPeriodoDeshabilitado() {
        crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                false
        );

        boolean resultado = repository.existeSolapamientoHabilitado(
                idAlojamiento,
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 12, 5)
        );

        assertFalse(resultado);
    }

    @Test
    void noExisteSolapamientoEnOtroAlojamiento() {
        crearPeriodo(
                idOtroAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado = repository.existeSolapamientoHabilitado(
                idAlojamiento,
                LocalDate.of(2026, 11, 25),
                LocalDate.of(2026, 12, 5)
        );

        assertFalse(resultado);
    }

    @Test
    void existeSolapamientoExcluyendoPeriodo() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado =
                repository.existeSolapamientoExcluyendoPeriodo(
                        idAlojamiento,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertTrue(resultado);
    }

    @Test
    void noExisteSolapamientoAlExcluirElMismoPeriodo() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                true
        );

        boolean resultado =
                repository.existeSolapamientoExcluyendoPeriodo(
                        idAlojamiento,
                        periodo.getIdPeriodo(),
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertFalse(resultado);
    }

    @Test
    void noExisteSolapamientoExcluyendoPeriodoDeshabilitado() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                idAlojamiento,
                LocalDate.of(2026, 11, 20),
                LocalDate.of(2026, 11, 30),
                false
        );

        boolean resultado =
                repository.existeSolapamientoExcluyendoPeriodo(
                        idAlojamiento,
                        UUID.randomUUID(),
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 12, 5)
                );

        assertFalse(resultado);
    }
}
