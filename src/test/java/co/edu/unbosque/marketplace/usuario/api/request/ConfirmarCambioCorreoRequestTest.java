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

class ConfirmarCambioCorreoRequestTest {

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
    void constructorInicializaCodigoEnNull() {
        ConfirmarCambioCorreoRequest request =
                new ConfirmarCambioCorreoRequest();

        assertNull(request.getCodigo());
    }

    @Test
    void getterDevuelveCodigoAsignado() {
        ConfirmarCambioCorreoRequest request =
                new ConfirmarCambioCorreoRequest();
        request.setCodigo("123456");

        assertEquals("123456", request.getCodigo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "000000", "012345", "999999"})
    void aceptaCodigoDeSeisDigitos(String codigo) {
        ConfirmarCambioCorreoRequest request =
                new ConfirmarCambioCorreoRequest();
        request.setCodigo(codigo);

        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaCodigoNuloVacioOEnBlanco(String codigo) {
        ConfirmarCambioCorreoRequest request =
                new ConfirmarCambioCorreoRequest();
        request.setCodigo(codigo);

        var errores = validator.validate(request);

        // Un valor vacío o en blanco también puede incumplir @Pattern.
        assertTrue(errores.stream().anyMatch(error ->
            "codigo".equals(error.getPropertyPath().toString())
                && "Debes proporcionar el código.".equals(error.getMessage())
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1",
        "12345",
        "1234567",
        "abcdef",
        "12345a",
        "123 56",
        " 123456",
        "123456 ",
        "-12345",
        "123456\n"
    })
    void rechazaCodigoConFormatoInvalido(String codigo) {
        ConfirmarCambioCorreoRequest request =
                new ConfirmarCambioCorreoRequest();
        request.setCodigo(codigo);

        var errores = validator.validate(request);

        assertEquals(1, errores.size());
        var error = errores.iterator().next();

        assertAll(
            () -> assertEquals(
                "codigo", error.getPropertyPath().toString()
            ),
            () -> assertEquals(
                "El código debe contener exactamente 6 dígitos.",
                error.getMessage()
            )
        );
    }
}
