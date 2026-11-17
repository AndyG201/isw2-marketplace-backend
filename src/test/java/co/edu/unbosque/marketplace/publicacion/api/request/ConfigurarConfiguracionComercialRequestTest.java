package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ConfigurarConfiguracionComercialRequestTest {

    private Validator validator;
    private ConfigurarConfiguracionComercialRequest request;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory()
                .getValidator();

        request = new ConfigurarConfiguracionComercialRequest();
    }

    @Test
    void testGettersYSetters() {
        BigDecimal tarifa = new BigDecimal("25000.00");
        List<ServicioAdicionalRequest> servicios = new ArrayList<>();

        request.setTarifaLimpieza(tarifa);
        request.setServiciosAdicionales(servicios);

        assertEquals(tarifa, request.getTarifaLimpieza());
        assertEquals(servicios, request.getServiciosAdicionales());
    }

    @Test
    void testTarifaLimpiezaValida() {
        request.setTarifaLimpieza(new BigDecimal("50000.00"));
        request.setServiciosAdicionales(new ArrayList<>());

        Set<ConstraintViolation<ConfigurarConfiguracionComercialRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTarifaLimpiezaCero() {
        request.setTarifaLimpieza(BigDecimal.ZERO);
        request.setServiciosAdicionales(new ArrayList<>());

        Set<ConstraintViolation<ConfigurarConfiguracionComercialRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTarifaLimpiezaNula() {
        request.setTarifaLimpieza(null);
        request.setServiciosAdicionales(new ArrayList<>());

        Set<ConstraintViolation<ConfigurarConfiguracionComercialRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "La tarifa de limpieza es obligatoria.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void testTarifaLimpiezaNegativa() {
        request.setTarifaLimpieza(new BigDecimal("-1000.00"));
        request.setServiciosAdicionales(new ArrayList<>());

        Set<ConstraintViolation<ConfigurarConfiguracionComercialRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals(
                        "La tarifa de limpieza no puede ser negativa."
                )
        ));
    }

    @Test
    void testListaServiciosAdicionalesNula() {
        request.setTarifaLimpieza(new BigDecimal("10000.00"));
        request.setServiciosAdicionales(null);

        Set<ConstraintViolation<ConfigurarConfiguracionComercialRequest>> violations =
                validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals(
                "La lista de servicios adicionales es obligatoria.",
                violations.iterator().next().getMessage()
        );
    }

    @Test
    void testListaServiciosAdicionalesVacia() {
        request.setTarifaLimpieza(new BigDecimal("10000.00"));
        request.setServiciosAdicionales(new ArrayList<>());

        Set<ConstraintViolation<ConfigurarConfiguracionComercialRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testTodosLosCamposNulos() {
        request.setTarifaLimpieza(null);
        request.setServiciosAdicionales(null);

        Set<ConstraintViolation<ConfigurarConfiguracionComercialRequest>> violations =
                validator.validate(request);

        assertEquals(2, violations.size());
    }
}