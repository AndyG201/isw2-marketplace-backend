package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RechazarPublicacionRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    @Test
    void testMotivoValido() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();
        request.setMotivo("La publicación no cumple con las políticas.");

        Set<ConstraintViolation<RechazarPublicacionRequest>> errores =
            validator.validate(request);

        assertTrue(errores.isEmpty());
    }

    @Test
    void testMotivoObligatorio() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();

        Set<ConstraintViolation<RechazarPublicacionRequest>> errores =
            validator.validate(request);

        assertTrue(errores.stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("motivo")
        ));
    }

    @Test
    void testMotivoNoPuedeEstarVacio() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();
        request.setMotivo("");

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("motivo")
        ));
    }

    @Test
    void testMotivoNoPuedeContenerSoloEspacios() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();
        request.setMotivo("     ");

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("motivo")
        ));
    }

    @Test
    void testMotivoConLongitudMaximaPermitida() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();
        request.setMotivo("A".repeat(2000));

        assertFalse(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("motivo")
        ));
    }

    @Test
    void testMotivoSuperaLongitudMaxima() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();
        request.setMotivo("A".repeat(2001));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("motivo")
        ));
    }

    @Test
    void testConstructorVacio() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();

        assertEquals(null, request.getMotivo());
    }

    @Test
    void testGettersYSetters() {
        RechazarPublicacionRequest request = new RechazarPublicacionRequest();

        String motivo = "La publicación contiene información incompleta.";

        request.setMotivo(motivo);

        assertEquals(motivo, request.getMotivo());
    }
}
