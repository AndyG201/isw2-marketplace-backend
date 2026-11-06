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

class ActualizarContrasenaRequestTest {

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

    private ActualizarContrasenaRequest solicitudValida() {
        ActualizarContrasenaRequest request =
                new ActualizarContrasenaRequest();

        request.setContrasenaActual("ClaveActual123");
        request.setNuevaContrasena("NuevaClave456");
        return request;
    }

    @Test
    void constructorInicializaCamposEnNull() {
        ActualizarContrasenaRequest request =
                new ActualizarContrasenaRequest();

        assertAll(
            () -> assertNull(request.getContrasenaActual()),
            () -> assertNull(request.getNuevaContrasena())
        );
    }

    @Test
    void gettersDevuelvenValoresAsignados() {
        ActualizarContrasenaRequest request = solicitudValida();

        assertAll(
            () -> assertEquals(
                "ClaveActual123", request.getContrasenaActual()
            ),
            () -> assertEquals(
                "NuevaClave456", request.getNuevaContrasena()
            )
        );
    }

    @Test
    void aceptaSolicitudValida() {
        assertTrue(validator.validate(solicitudValida()).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaContrasenaActualNulaVaciaOEnBlanco(String valor) {
        ActualizarContrasenaRequest request = solicitudValida();
        request.setContrasenaActual(valor);

        var errores = validator.validate(request);

        assertEquals(1, errores.size());
        var error = errores.iterator().next();

        assertAll(
            () -> assertEquals(
                "contrasenaActual", error.getPropertyPath().toString()
            ),
            () -> assertEquals(
                "Debes proporcionar tu contraseña actual.",
                error.getMessage()
            )
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaNuevaContrasenaNulaVaciaOEnBlanco(String valor) {
        ActualizarContrasenaRequest request = solicitudValida();
        request.setNuevaContrasena(valor);

        var errores = validator.validate(request);

        assertEquals(1, errores.size());
        var error = errores.iterator().next();

        assertAll(
            () -> assertEquals(
                "nuevaContrasena", error.getPropertyPath().toString()
            ),
            () -> assertEquals(
                "Debes proporcionar la nueva contraseña.",
                error.getMessage()
            )
        );
    }

    @Test
    void rechazaAmbasContrasenasAusentes() {
        ActualizarContrasenaRequest request =
                new ActualizarContrasenaRequest();

        var errores = validator.validate(request);

        assertEquals(2, errores.size());
        assertAll(
            () -> assertTrue(errores.stream().anyMatch(error ->
                "contrasenaActual".equals(
                    error.getPropertyPath().toString()
                )
            )),
            () -> assertTrue(errores.stream().anyMatch(error ->
                "nuevaContrasena".equals(
                    error.getPropertyPath().toString()
                )
            ))
        );
    }
}
