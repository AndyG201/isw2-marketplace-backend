package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class SeleccionarPoliticaCancelacionRequestTest {

    private Validator validator;
    private SeleccionarPoliticaCancelacionRequest request;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        request = new SeleccionarPoliticaCancelacionRequest();
    }

    @Test
    void testGettersYSetters() {
        UUID id = UUID.randomUUID();
        request.setIdPolitica(id);

        assertEquals(id, request.getIdPolitica());
    }

    @Test
    void testIdPoliticaValido() {
        request.setIdPolitica(UUID.randomUUID());

        Set<ConstraintViolation<SeleccionarPoliticaCancelacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testIdPoliticaNulo() {
        request.setIdPolitica(null);

        Set<ConstraintViolation<SeleccionarPoliticaCancelacionRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "Debes seleccionar una política de cancelación."
                )
        ));
    }

    @Test
    void testConstructorVacio() {
        assertEquals(null, request.getIdPolitica());
    }
}
