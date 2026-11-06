package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SolicitarRecuperacionRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    @Test
    void constructorInicializaCorreoEnNull() {
        SolicitarRecuperacionRequest request =
                new SolicitarRecuperacionRequest();

        assertNull(request.getCorreo());
    }

    @Test
    void getterDevuelveCorreoAsignado() {
        SolicitarRecuperacionRequest request =
                new SolicitarRecuperacionRequest();
        request.setCorreo("usuario@ejemplo.com");

        assertEquals("usuario@ejemplo.com", request.getCorreo());
    }

    @Test
    void aceptaSolicitudValida() {
        SolicitarRecuperacionRequest request =
                new SolicitarRecuperacionRequest();
        request.setCorreo("usuario@ejemplo.com");

        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaCorreoNuloVacioOEnBlanco(String correo) {
        SolicitarRecuperacionRequest request =
                new SolicitarRecuperacionRequest();
        request.setCorreo(correo);

        verificarError(request, "Debes proporcionar un correo.");
    }

    @Test
    void aceptaCorreoDe254Caracteres() {
        SolicitarRecuperacionRequest request =
                new SolicitarRecuperacionRequest();
        request.setCorreo("a".repeat(242) + "@ejemplo.com");

        assertEquals(254, request.getCorreo().length());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaCorreoDe255Caracteres() {
        SolicitarRecuperacionRequest request =
                new SolicitarRecuperacionRequest();
        request.setCorreo("a".repeat(243) + "@ejemplo.com");

        verificarError(
            request,
            "El correo supera la longitud permitida."
        );
    }

    private void verificarError(
            SolicitarRecuperacionRequest request,
            String mensaje) {

        var errores = validator.validate(request);

        assertEquals(1, errores.size());
        var error = errores.iterator().next();

        assertAll(
            () -> assertEquals(
                "correo", error.getPropertyPath().toString()
            ),
            () -> assertEquals(mensaje, error.getMessage())
        );
    }
}
