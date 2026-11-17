package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ConfigurarModoAceptacionRequestTest {

    private Validator validator;
    private ConfigurarModoAceptacionRequest request;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        request = new ConfigurarModoAceptacionRequest();
    }

    @Test
    void testGettersYSetters() {
        request.setModoAceptacion("INMEDIATA");

        assertEquals("INMEDIATA", request.getModoAceptacion());
    }

    @Test
    void testModoInmediataValido() {
        request.setModoAceptacion("INMEDIATA");

        Set<ConstraintViolation<ConfigurarModoAceptacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testModoAprobacionValido() {
        request.setModoAceptacion("APROBACION");

        Set<ConstraintViolation<ConfigurarModoAceptacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testModoAceptacionVacio() {
        request.setModoAceptacion("");

        Set<ConstraintViolation<ConfigurarModoAceptacionRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
    }

    @Test
    void testModoAceptacionInvalido() {
        request.setModoAceptacion("AUTOMATICO");

        Set<ConstraintViolation<ConfigurarModoAceptacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "El modo debe ser INMEDIATA o APROBACION."
                )
        ));
    }

    @Test
    void testModoAceptacionNulo() {
        request.setModoAceptacion(null);

        Set<ConstraintViolation<ConfigurarModoAceptacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "El modo de aceptación es obligatorio."
                )
        ));
    }
}
