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

class RestablecerContrasenaRequestTest {

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

    private RestablecerContrasenaRequest solicitudValida() {
        RestablecerContrasenaRequest request =
                new RestablecerContrasenaRequest();

        request.setCorreo("usuario@ejemplo.com");
        request.setCodigo("123456");
        request.setNuevaContrasena("NuevaClave123");
        return request;
    }

    @Test
    void constructorInicializaCamposEnNull() {
        RestablecerContrasenaRequest request =
                new RestablecerContrasenaRequest();

        assertAll(
            () -> assertNull(request.getCorreo()),
            () -> assertNull(request.getCodigo()),
            () -> assertNull(request.getNuevaContrasena())
        );
    }

    @Test
    void gettersDevuelvenValoresAsignados() {
        RestablecerContrasenaRequest request = solicitudValida();

        assertAll(
            () -> assertEquals(
                "usuario@ejemplo.com", request.getCorreo()
            ),
            () -> assertEquals("123456", request.getCodigo()),
            () -> assertEquals(
                "NuevaClave123", request.getNuevaContrasena()
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
    void rechazaCorreoNuloVacioOEnBlanco(String correo) {
        RestablecerContrasenaRequest request = solicitudValida();
        request.setCorreo(correo);

        verificarError(
            request, "correo", "Debes proporcionar un correo."
        );
    }

    @Test
    void aceptaCorreoDe254Caracteres() {
        RestablecerContrasenaRequest request = solicitudValida();
        request.setCorreo("a".repeat(242) + "@ejemplo.com");

        assertEquals(254, request.getCorreo().length());
        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaCorreoDe255Caracteres() {
        RestablecerContrasenaRequest request = solicitudValida();
        request.setCorreo("a".repeat(243) + "@ejemplo.com");

        verificarError(
            request,
            "correo",
            "El correo supera la longitud permitida."
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaCodigoNuloVacioOEnBlanco(String codigo) {
        RestablecerContrasenaRequest request = solicitudValida();
        request.setCodigo(codigo);

        var errores = validator.validate(request);

        // El código vacío o en blanco también incumple @Pattern.
        assertTrue(errores.stream().anyMatch(error ->
            "codigo".equals(error.getPropertyPath().toString())
                && "Debes proporcionar el código.".equals(
                    error.getMessage()
                )
        ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456", "000000", "012345", "999999"})
    void aceptaCodigoDeSeisDigitos(String codigo) {
        RestablecerContrasenaRequest request = solicitudValida();
        request.setCodigo(codigo);

        assertTrue(validator.validate(request).isEmpty());
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
        RestablecerContrasenaRequest request = solicitudValida();
        request.setCodigo(codigo);

        verificarError(
            request,
            "codigo",
            "El código debe contener exactamente 6 dígitos."
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaNuevaContrasenaNulaVaciaOEnBlanco(String contrasena) {
        RestablecerContrasenaRequest request = solicitudValida();
        request.setNuevaContrasena(contrasena);

        verificarError(
            request,
            "nuevaContrasena",
            "Debes proporcionar la nueva contraseña."
        );
    }

    @Test
    void rechazaTodosLosCamposAusentes() {
        RestablecerContrasenaRequest request =
                new RestablecerContrasenaRequest();

        var errores = validator.validate(request);

        assertEquals(3, errores.size());
        assertAll(
            () -> assertTrue(errores.stream().anyMatch(error ->
                "correo".equals(error.getPropertyPath().toString())
            )),
            () -> assertTrue(errores.stream().anyMatch(error ->
                "codigo".equals(error.getPropertyPath().toString())
            )),
            () -> assertTrue(errores.stream().anyMatch(error ->
                "nuevaContrasena".equals(
                    error.getPropertyPath().toString()
                )
            ))
        );
    }

    private void verificarError(
            RestablecerContrasenaRequest request,
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
