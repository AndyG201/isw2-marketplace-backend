package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacionRequestTest {

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

    private AutenticacionRequest solicitudValida() {
        AutenticacionRequest request = new AutenticacionRequest();
        request.setCorreo("usuario@ejemplo.com");
        request.setContrasena("Clave123");
        return request;
    }

    @Test
    void constructorInicializaCamposEnNull() {
        AutenticacionRequest request = new AutenticacionRequest();

        assertNull(request.getCorreo());
        assertNull(request.getContrasena());
    }

    @Test
    void gettersDevuelvenValoresAsignados() {
        AutenticacionRequest request = solicitudValida();

        assertEquals("usuario@ejemplo.com", request.getCorreo());
        assertEquals("Clave123", request.getContrasena());
    }

    @Test
    void aceptaSolicitudValida() {
        assertTrue(validator.validate(solicitudValida()).isEmpty());
    }

    @Test
    void rechazaCorreoNuloVacioOEnBlanco() {
        for (String correo : new String[]{null, "", "   "}) {
            AutenticacionRequest request = solicitudValida();
            request.setCorreo(correo);

            var errores = validator.validate(request);

            assertEquals(1, errores.size());
            var error = errores.iterator().next();
            assertEquals("correo", error.getPropertyPath().toString());
            assertEquals("El correo es obligatorio.", error.getMessage());
        }
    }

    @Test
    void aceptaCorreoDe254Caracteres() {
        AutenticacionRequest request = solicitudValida();
        request.setCorreo("a".repeat(242) + "@ejemplo.com");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaCorreoDe255Caracteres() {
        AutenticacionRequest request = solicitudValida();
        request.setCorreo("a".repeat(243) + "@ejemplo.com");

        var errores = validator.validate(request);

        assertEquals(1, errores.size());
        var error = errores.iterator().next();
        assertEquals("correo", error.getPropertyPath().toString());
        assertEquals(
                "El correo supera la longitud permitida.",
                error.getMessage()
        );
    }

    @Test
    void rechazaContrasenaNulaVaciaOEnBlanco() {
        for (String contrasena : new String[]{null, "", "   "}) {
            AutenticacionRequest request = solicitudValida();
            request.setContrasena(contrasena);

            var errores = validator.validate(request);

            assertEquals(1, errores.size());
            var error = errores.iterator().next();
            assertEquals("contrasena", error.getPropertyPath().toString());
            assertEquals("La contraseña es obligatoria.", error.getMessage());
        }
    }
}
