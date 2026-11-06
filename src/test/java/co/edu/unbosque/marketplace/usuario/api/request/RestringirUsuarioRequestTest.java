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

class RestringirUsuarioRequestTest {

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
    void constructorInicializaMotivoEnNull() {
        RestringirUsuarioRequest request = new RestringirUsuarioRequest();

        assertNull(request.getMotivo());
    }

    @Test
    void getterDevuelveMotivoAsignado() {
        RestringirUsuarioRequest request = new RestringirUsuarioRequest();
        request.setMotivo("Incumplimiento de las normas.");

        assertEquals(
            "Incumplimiento de las normas.",
            request.getMotivo()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Incumplimiento de las normas.",
        "Comportamiento inapropiado.",
        "a"
    })
    void aceptaMotivoValido(String motivo) {
        RestringirUsuarioRequest request = new RestringirUsuarioRequest();
        request.setMotivo(motivo);

        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaMotivoNuloVacioOEnBlanco(String motivo) {
        RestringirUsuarioRequest request = new RestringirUsuarioRequest();
        request.setMotivo(motivo);

        verificarError(
            request,
            "El motivo de restricción es obligatorio."
        );
    }

    @Test
    void aceptaMotivoDe255Caracteres() {
        RestringirUsuarioRequest request = new RestringirUsuarioRequest();
        request.setMotivo("a".repeat(255));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rechazaMotivoDe256Caracteres() {
        RestringirUsuarioRequest request = new RestringirUsuarioRequest();
        request.setMotivo("a".repeat(256));

        verificarError(
            request,
            "El motivo no puede superar 255 caracteres."
        );
    }

    private void verificarError(
            RestringirUsuarioRequest request,
            String mensaje) {

        var errores = validator.validate(request);

        assertEquals(1, errores.size());
        var error = errores.iterator().next();

        assertAll(
            () -> assertEquals(
                "motivo", error.getPropertyPath().toString()
            ),
            () -> assertEquals(mensaje, error.getMessage())
        );
    }
}
