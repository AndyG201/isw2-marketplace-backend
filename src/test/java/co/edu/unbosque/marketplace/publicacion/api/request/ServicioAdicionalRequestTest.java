package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ServicioAdicionalRequestTest {

    private Validator validator;
    private ServicioAdicionalRequest request;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        request = new ServicioAdicionalRequest();
    }

    @Test
    void testGettersYSetters() {
        request.setNombre("Piscina");
        request.setDescripcion("Acceso a piscina.");
        request.setCosto(new BigDecimal("25000.00"));

        assertEquals("Piscina", request.getNombre());
        assertEquals("Acceso a piscina.", request.getDescripcion());
        assertEquals(new BigDecimal("25000.00"), request.getCosto());
    }

    @Test
    void testDatosValidos() {
        request.setNombre("Piscina");
        request.setDescripcion("Acceso a piscina.");
        request.setCosto(new BigDecimal("25000.00"));

        Set<ConstraintViolation<ServicioAdicionalRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testNombreNuloOVacio() {
        request.setNombre(" ");
        request.setCosto(BigDecimal.ZERO);

        Set<ConstraintViolation<ServicioAdicionalRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "El nombre del servicio es obligatorio."
                )
        ));
    }

    @Test
    void testNombreSupera100Caracteres() {
        request.setNombre("A".repeat(101));
        request.setCosto(BigDecimal.ZERO);

        Set<ConstraintViolation<ServicioAdicionalRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "El nombre no puede superar 100 caracteres."
                )
        ));
    }

    @Test
    void testCostoNulo() {
        request.setNombre("Piscina");
        request.setCosto(null);

        Set<ConstraintViolation<ServicioAdicionalRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "El costo del servicio es obligatorio."
                )
        ));
    }

    @Test
    void testCostoNegativo() {
        request.setNombre("Piscina");
        request.setCosto(new BigDecimal("-1000.00"));

        Set<ConstraintViolation<ServicioAdicionalRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "El costo del servicio no puede ser negativo."
                )
        ));
    }

    @Test
    void testCostoCeroValido() {
        request.setNombre("Piscina");
        request.setCosto(BigDecimal.ZERO);

        Set<ConstraintViolation<ServicioAdicionalRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
