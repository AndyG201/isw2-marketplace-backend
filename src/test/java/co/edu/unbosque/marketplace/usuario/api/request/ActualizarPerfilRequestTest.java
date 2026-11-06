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

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class ActualizarPerfilRequestTest {

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

    private ActualizarPerfilRequest solicitudValida() {
        ActualizarPerfilRequest request = new ActualizarPerfilRequest();
        request.setNombre("Ana");
        request.setApellido("Perez");
        request.setCorreo("ana@ejemplo.com");
        return request;
    }

    @Test
    void constructorInicializaCamposEnNull() {
        ActualizarPerfilRequest request = new ActualizarPerfilRequest();

        assertAll(
            () -> assertNull(request.getNombre()),
            () -> assertNull(request.getApellido()),
            () -> assertNull(request.getCorreo())
        );
    }

    @Test
    void gettersDevuelvenValoresAsignados() {
        ActualizarPerfilRequest request = solicitudValida();

        assertAll(
            () -> assertEquals("Ana", request.getNombre()),
            () -> assertEquals("Perez", request.getApellido()),
            () -> assertEquals("ana@ejemplo.com", request.getCorreo())
        );
    }

    @Test
    void aceptaSolicitudValida() {
        assertTrue(validator.validate(solicitudValida()).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaNombreNuloVacioOEnBlanco(String valor) {
        ActualizarPerfilRequest request = solicitudValida();
        request.setNombre(valor);

        verificarError(
            request, "nombre", "El nombre es obligatorio."
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaApellidoNuloVacioOEnBlanco(String valor) {
        ActualizarPerfilRequest request = solicitudValida();
        request.setApellido(valor);

        verificarError(
            request, "apellido", "El apellido es obligatorio."
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rechazaCorreoNuloVacioOEnBlanco(String valor) {
        ActualizarPerfilRequest request = solicitudValida();
        request.setCorreo(valor);

        verificarError(
            request, "correo", "El correo es obligatorio."
        );
    }

    @Test
    void validaLongitudDelNombre() {
        verificarLongitud(
            ActualizarPerfilRequest::setNombre,
            "nombre",
            50,
            "El nombre no puede superar 50 caracteres."
        );
    }

    @Test
    void validaLongitudDelApellido() {
        verificarLongitud(
            ActualizarPerfilRequest::setApellido,
            "apellido",
            50,
            "El apellido no puede superar 50 caracteres."
        );
    }

    @Test
    void validaLongitudDelCorreo() {
        verificarLongitud(
            ActualizarPerfilRequest::setCorreo,
            "correo",
            254,
            "El correo supera la longitud permitida."
        );
    }

    @Test
    void rechazaTodosLosCamposAusentes() {
        ActualizarPerfilRequest request = new ActualizarPerfilRequest();

        var errores = validator.validate(request);

        assertEquals(3, errores.size());
        assertAll(
            () -> assertTrue(errores.stream().anyMatch(error ->
                "nombre".equals(error.getPropertyPath().toString())
            )),
            () -> assertTrue(errores.stream().anyMatch(error ->
                "apellido".equals(error.getPropertyPath().toString())
            )),
            () -> assertTrue(errores.stream().anyMatch(error ->
                "correo".equals(error.getPropertyPath().toString())
            ))
        );
    }

    private void verificarLongitud(
            BiConsumer<ActualizarPerfilRequest, String> setter,
            String campo,
            int maximo,
            String mensaje) {

        ActualizarPerfilRequest request = solicitudValida();

        setter.accept(request, "a".repeat(maximo));
        assertTrue(
            validator.validate(request).isEmpty(),
            campo + " debe aceptar " + maximo + " caracteres"
        );

        setter.accept(request, "a".repeat(maximo + 1));
        verificarError(request, campo, mensaje);
    }

    private void verificarError(
            ActualizarPerfilRequest request,
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