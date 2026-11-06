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

class ReactivarCuentaRequestTest {

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

    private ReactivarCuentaRequest solicitudValida() {
        ReactivarCuentaRequest request = new ReactivarCuentaRequest();
        request.setCorreo("usuario@ejemplo.com");
        request.setContrasena("Clave123");
        return request;
    }

    @Test
    void constructorInicializaCamposEnNull() {
        ReactivarCuentaRequest request = new ReactivarCuentaRequest();

        assertAll(
            () -> assertNull(request.getCorreo()),
            () -> assertNull(request.getContrasena())
        );
    }

    @Test
    void gettersDevuelvenValoresAsignados() {
        ReactivarCuentaRequest request = solicitudValida();

        assertAll(
            () -> assertEquals(
                "usuario@ejemplo.com", request.getCorreo()
            ),
            () -> assertEquals("Clave123", request.getContrasena())
        );
    }

    @Test
    void aceptaSolicitudValida() {
        assertTrue(validator.validate(solicitudValida()).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaCorreoNuloVacioOEnBlanco(String correo) {
        ReactivarCuentaRequest request = solicitudValida();
        request.setCorreo(correo);

        verificarError(
            request, "correo", "El correo es obligatorio."
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaContrasenaNulaVaciaOEnBlanco(String contrasena) {
        ReactivarCuentaRequest request = solicitudValida();
        request.setContrasena(contrasena);

        verificarError(
            request, "contrasena", "La contraseña es obligatoria."
        );
    }

    @Test
    void aceptaCorreoDe254Caracteres() {
        ReactivarCuentaRequest request = solicitudValida();
        request.setCorreo("a".repeat(242) + "@ejemplo.com");

        assertEquals(254, request.getCorreo().length());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaCorreoDe255Caracteres() {
        ReactivarCuentaRequest request = solicitudValida();
        request.setCorreo("a".repeat(243) + "@ejemplo.com");

        verificarError(
            request,
            "correo",
            "El correo supera la longitud permitida."
        );
    }

    @Test
    void rechazaAmbosCamposAusentes() {
        ReactivarCuentaRequest request = new ReactivarCuentaRequest();

        var errores = validator.validate(request);

        assertEquals(2, errores.size());
        assertAll(
            () -> assertTrue(errores.stream().anyMatch(error ->
                "correo".equals(error.getPropertyPath().toString())
                    && "El correo es obligatorio.".equals(
                        error.getMessage()
                    )
            )),
            () -> assertTrue(errores.stream().anyMatch(error ->
                "contrasena".equals(error.getPropertyPath().toString())
                    && "La contraseña es obligatoria.".equals(
                        error.getMessage()
                    )
            ))
        );
    }

    private void verificarError(
            ReactivarCuentaRequest request,
            String campo,
            String mensaje) {

        var errores = validator.validate(request);

        assertEquals(1, errores.size());
        var error = errores.iterator().next();

        assertAll(
            () -> assertEquals(
                campo, error.getPropertyPath().toString()
            ),
            () -> assertEquals(mensaje, error.getMessage())
        );
    }
}
