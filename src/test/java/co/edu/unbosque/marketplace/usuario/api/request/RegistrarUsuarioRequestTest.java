package co.edu.unbosque.marketplace.usuario.api.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.*;

class RegistrarUsuarioRequestTest {

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

    private RegistrarUsuarioRequest solicitudValida() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest();
        request.setNombre("Ana");
        request.setApellido("Perez");
        request.setCorreo("ana@ejemplo.com");
        request.setTipoDocumento("CEDULA");
        request.setDocumentoIdentidad("123456789");
        request.setTelefono("3001234567");
        request.setContrasena("Clave123");
        request.setRol("HUESPED");
        return request;
    }

    @Test
    void constructorInicializaCamposEnNull() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest();

        assertAll(
            () -> assertNull(request.getNombre()),
            () -> assertNull(request.getApellido()),
            () -> assertNull(request.getCorreo()),
            () -> assertNull(request.getTipoDocumento()),
            () -> assertNull(request.getDocumentoIdentidad()),
            () -> assertNull(request.getTelefono()),
            () -> assertNull(request.getContrasena()),
            () -> assertNull(request.getRol())
        );
    }

    @Test
    void gettersDevuelvenValoresAsignados() {
        RegistrarUsuarioRequest request = solicitudValida();

        assertAll(
            () -> assertEquals("Ana", request.getNombre()),
            () -> assertEquals("Perez", request.getApellido()),
            () -> assertEquals("ana@ejemplo.com", request.getCorreo()),
            () -> assertEquals("CEDULA", request.getTipoDocumento()),
            () -> assertEquals("123456789", request.getDocumentoIdentidad()),
            () -> assertEquals("3001234567", request.getTelefono()),
            () -> assertEquals("Clave123", request.getContrasena()),
            () -> assertEquals("HUESPED", request.getRol())
        );
    }

    @Test
    void aceptaSolicitudValida() {
        assertTrue(validator.validate(solicitudValida()).isEmpty());
    }

    @Test
    void validaCamposObligatorios() {
        assertAll(
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setNombre,
                "nombre", "El nombre es obligatorio."
            ),
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setApellido,
                "apellido", "El apellido es obligatorio."
            ),
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setCorreo,
                "correo", "El correo es obligatorio."
            ),
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setTipoDocumento,
                "tipoDocumento", "El tipo de documento es obligatorio."
            ),
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setDocumentoIdentidad,
                "documentoIdentidad",
                "El documento de identidad es obligatorio."
            ),
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setTelefono,
                "telefono", "El teléfono es obligatorio."
            ),
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setContrasena,
                "contrasena", "La contraseña es obligatoria."
            ),
            () -> verificarObligatorio(
                RegistrarUsuarioRequest::setRol,
                "rol", "El rol es obligatorio."
            )
        );
    }

    @Test
    void validaLongitudDelNombre() {
        verificarLongitud(
            RegistrarUsuarioRequest::setNombre,
            "nombre", 50,
            "El nombre no puede superar 50 caracteres."
        );
    }

    @Test
    void validaLongitudDelApellido() {
        verificarLongitud(
            RegistrarUsuarioRequest::setApellido,
            "apellido", 50,
            "El apellido no puede superar 50 caracteres."
        );
    }

    @Test
    void validaLongitudDelCorreo() {
        verificarLongitud(
            RegistrarUsuarioRequest::setCorreo,
            "correo", 254,
            "El correo supera la longitud permitida."
        );
    }

    @Test
    void validaLongitudDelTipoDocumento() {
        verificarLongitud(
            RegistrarUsuarioRequest::setTipoDocumento,
            "tipoDocumento", 30,
            "El tipo de documento supera la longitud permitida."
        );
    }

    @Test
    void validaLongitudDelTelefono() {
        verificarLongitud(
            RegistrarUsuarioRequest::setTelefono,
            "telefono", 20,
            "El teléfono supera la longitud permitida."
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "HUESPED",
        "ANFITRION",
        "huesped",
        "anfitrion",
        "HuEsPeD",
        "AnFiTrIoN",
        " HUESPED ",
        "  ANFITRION  ",
        "\tHUESPED\n"
    })
    void aceptaRolesPermitidos(String rol) {
        RegistrarUsuarioRequest request = solicitudValida();
        request.setRol(rol);

        assertTrue(validator.validate(request).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ADMIN",
        "USUARIO",
        "HUESPED ANFITRION",
        "HUES PED",
        "ANFITRION123"
    })
    void rechazaRolesInvalidos(String rol) {
        RegistrarUsuarioRequest request = solicitudValida();
        request.setRol(rol);

        verificarError(
            request,
            "rol",
            "El rol debe ser HUESPED o ANFITRION."
        );
    }

    @Test
    void rechazaTodosLosCamposAusentes() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest();

        // @Pattern y @Size aceptan null; cada @NotBlank genera un error.
        assertEquals(8, validator.validate(request).size());
    }

    private void verificarObligatorio(
            BiConsumer<RegistrarUsuarioRequest, String> setter,
            String campo,
            String mensaje) {

        for (String valor : new String[]{null, "", "   ", "\t", "\n"}) {
            RegistrarUsuarioRequest request = solicitudValida();
            setter.accept(request, valor);

            var errores = validator.validate(request);

            // El rol vacío o en blanco también incumple @Pattern.
            assertTrue(
                errores.stream().anyMatch(error ->
                    campo.equals(error.getPropertyPath().toString())
                        && mensaje.equals(error.getMessage())
                ),
                "Se esperaba la validación obligatoria de " + campo
            );
        }
    }

    private void verificarLongitud(
            BiConsumer<RegistrarUsuarioRequest, String> setter,
            String campo,
            int maximo,
            String mensaje) {

        RegistrarUsuarioRequest request = solicitudValida();

        setter.accept(request, "a".repeat(maximo));
        assertTrue(
            validator.validate(request).isEmpty(),
            campo + " debe aceptar " + maximo + " caracteres"
        );

        setter.accept(request, "a".repeat(maximo + 1));
        verificarError(request, campo, mensaje);
    }

    private void verificarError(
            RegistrarUsuarioRequest request,
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
