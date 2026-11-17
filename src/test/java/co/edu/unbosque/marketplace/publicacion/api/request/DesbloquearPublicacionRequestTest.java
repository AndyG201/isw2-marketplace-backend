package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class DesbloquearPublicacionRequestTest {

    private Validator validator;
    private DesbloquearPublicacionRequest request;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        request = new DesbloquearPublicacionRequest();
    }

    @Test
    void testGettersYSetters() {
        request.setMotivo("Se solucionó el incumplimiento.");

        assertEquals("Se solucionó el incumplimiento.", request.getMotivo());
    }

    @Test
    void testMotivoValido() {
        request.setMotivo("La publicación cumple nuevamente las políticas.");

        Set<ConstraintViolation<DesbloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testMotivoNulo() {
        request.setMotivo(null);

        Set<ConstraintViolation<DesbloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "Debes proporcionar el motivo del desbloqueo."
                )
        ));
    }

    @Test
    void testMotivoVacio() {
        request.setMotivo("");

        Set<ConstraintViolation<DesbloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "Debes proporcionar el motivo del desbloqueo."
                )
        ));
    }

    @Test
    void testMotivoCon2000Caracteres() {
        request.setMotivo("A".repeat(2000));

        Set<ConstraintViolation<DesbloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testMotivoSupera2000Caracteres() {
        request.setMotivo("A".repeat(2001));

        Set<ConstraintViolation<DesbloquearPublicacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "El motivo no puede superar los 2000 caracteres."
                )
        ));
    }
}
