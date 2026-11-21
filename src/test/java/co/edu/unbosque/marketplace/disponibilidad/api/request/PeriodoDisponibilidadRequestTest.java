package co.edu.unbosque.marketplace.disponibilidad.api.request;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PeriodoDisponibilidadRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void gettersYSettersFuncionanCorrectamente() {
        PeriodoDisponibilidadRequest request =
                new PeriodoDisponibilidadRequest();

        LocalDate fechaInicio = LocalDate.of(2026, 12, 10);
        LocalDate fechaFin = LocalDate.of(2026, 12, 15);

        request.setFechaInicio(fechaInicio);
        request.setFechaFin(fechaFin);

        assertEquals(fechaInicio, request.getFechaInicio());
        assertEquals(fechaFin, request.getFechaFin());
    }

    @Test
    void fechasNulasGeneranViolacionesDeValidacion() {
        PeriodoDisponibilidadRequest request =
                new PeriodoDisponibilidadRequest();

        Set<jakarta.validation.ConstraintViolation<PeriodoDisponibilidadRequest>>
                violaciones = validator.validate(request);

        assertEquals(2, violaciones.size());
    }

    @Test
    void fechaInicioNulaGeneraUnaViolacion() {
        PeriodoDisponibilidadRequest request =
                new PeriodoDisponibilidadRequest();

        request.setFechaFin(LocalDate.of(2026, 12, 15));

        Set<jakarta.validation.ConstraintViolation<PeriodoDisponibilidadRequest>>
                violaciones = validator.validate(request);

        assertEquals(1, violaciones.size());
        assertEquals("La fecha de inicio es obligatoria.",
                violaciones.iterator().next().getMessage());
    }

    @Test
    void fechaFinNulaGeneraUnaViolacion() {
        PeriodoDisponibilidadRequest request =
                new PeriodoDisponibilidadRequest();

        request.setFechaInicio(LocalDate.of(2026, 12, 10));

        Set<jakarta.validation.ConstraintViolation<PeriodoDisponibilidadRequest>>
                violaciones = validator.validate(request);

        assertEquals(1, violaciones.size());
        assertEquals("La fecha de fin es obligatoria.",
                violaciones.iterator().next().getMessage());
    }

    @Test
    void fechasInformadasNoGeneranViolaciones() {
        PeriodoDisponibilidadRequest request =
                new PeriodoDisponibilidadRequest();

        request.setFechaInicio(LocalDate.of(2026, 12, 10));
        request.setFechaFin(LocalDate.of(2026, 12, 15));

        Set<jakarta.validation.ConstraintViolation<PeriodoDisponibilidadRequest>>
                violaciones = validator.validate(request);

        assertTrue(violaciones.isEmpty());
    }
}
