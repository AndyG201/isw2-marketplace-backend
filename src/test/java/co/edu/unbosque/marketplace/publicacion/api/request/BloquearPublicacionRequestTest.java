package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class BloquearPublicacionRequestTest {

    private Validator validator;
    private BloquearPublicacionRequest request;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        request = new BloquearPublicacionRequest();
    }

    @Test
    void testConstructorVacio() {
        assertEquals(null, request.getMotivo());
    }

    @Test
    void testGettersYSetters() {
        request.setMotivo("Incumplimiento de las políticas.");

        assertEquals("Incumplimiento de las políticas.", request.getMotivo());
    }

    @Test
    void testMotivoValido() {
        request.setMotivo("La publicación incumple las políticas establecidas.");

        Set<ConstraintViolation<BloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testMotivoNulo() {
        request.setMotivo(null);

        Set<ConstraintViolation<BloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals(
                        "Debes proporcionar el motivo del bloqueo.")));
    }

    @Test
    void testMotivoVacio() {
        request.setMotivo("");

        Set<ConstraintViolation<BloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals(
                        "Debes proporcionar el motivo del bloqueo.")));
    }

    @Test
    void testMotivoSoloEspacios() {
        request.setMotivo("     ");

        Set<ConstraintViolation<BloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals(
                        "Debes proporcionar el motivo del bloqueo.")));
    }

    @Test
    void testMotivoCon2000Caracteres() {
        request.setMotivo("A".repeat(2000));

        Set<ConstraintViolation<BloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testMotivoSupera2000Caracteres() {
        request.setMotivo("A".repeat(2001));

        Set<ConstraintViolation<BloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals(
                        "El motivo no puede superar los 2000 caracteres.")));
    }
}
