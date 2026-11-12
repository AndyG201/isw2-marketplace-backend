package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class EditarPublicacionRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        factory.close();
    }

    private EditarPublicacionRequest crearRequestValido() {
        EditarPublicacionRequest request = new EditarPublicacionRequest();

        request.setTitulo("Apartamento moderno");
        request.setDescripcion("Apartamento cómodo y bien ubicado.");
        request.setReglas("No fumar dentro del alojamiento.");
        request.setUbicacion("Calle 100 # 15-20");
        request.setCiudad("Bogotá");
        request.setTipoAlojamiento("Apartamento");
        request.setCapacidad(4);
        request.setPrecioPorNoche(new BigDecimal("150000"));
        request.setTarifaLimpieza(new BigDecimal("20000"));
        request.setModoAceptacion("INMEDIATA");
        request.setIdPolitica(UUID.randomUUID());
        request.setIdsServicios(List.of(UUID.randomUUID()));

        MultipartFile imagen = new MockMultipartFile(
            "imagen",
            "foto.jpg",
            "image/jpeg",
            new byte[] {1, 2, 3}
        );

        request.setImagenes(List.of(imagen));

        return request;
    }

    @Test
    void testRequestValido() {
        EditarPublicacionRequest request = crearRequestValido();

        Set<ConstraintViolation<EditarPublicacionRequest>> errores =
            validator.validate(request);

        assertTrue(errores.isEmpty());
    }

    @Test
    void testCamposObligatorios() {
        EditarPublicacionRequest request = new EditarPublicacionRequest();

        Set<ConstraintViolation<EditarPublicacionRequest>> errores =
            validator.validate(request);

        assertFalse(errores.isEmpty());

        assertTrue(errores.stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("titulo")
        ));

        assertTrue(errores.stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("descripcion")
        ));

        assertTrue(errores.stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("reglas")
        ));
    }

    @Test
    void testTituloSuperaLongitudMaxima() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setTitulo("A".repeat(201));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("titulo")
        ));
    }

    @Test
    void testUbicacionSuperaLongitudMaxima() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setUbicacion("A".repeat(256));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("ubicacion")
        ));
    }

    @Test
    void testCiudadSuperaLongitudMaxima() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setCiudad("A".repeat(101));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("ciudad")
        ));
    }

    @Test
    void testTipoAlojamientoSuperaLongitudMaxima() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setTipoAlojamiento("A".repeat(51));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("tipoAlojamiento")
        ));
    }

    @Test
    void testCapacidadDebeSerMayorQueCero() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setCapacidad(0);

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("capacidad")
        ));
    }

    @Test
    void testPrecioPorNocheDebeSerMayorQueCero() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setPrecioPorNoche(BigDecimal.ZERO);

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("precioPorNoche")
        ));
    }

    @Test
    void testTarifaLimpiezaNoPuedeSerNegativa() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setTarifaLimpieza(new BigDecimal("-1"));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("tarifaLimpieza")
        ));
    }

    @Test
    void testTarifaLimpiezaPuedeSerCero() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setTarifaLimpieza(BigDecimal.ZERO);

        assertFalse(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("tarifaLimpieza")
        ));
    }

    @Test
    void testModoAceptacionInmediataEsValido() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setModoAceptacion("INMEDIATA");

        assertFalse(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("modoAceptacion")
        ));
    }

    @Test
    void testModoAceptacionAprobacionEsValido() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setModoAceptacion("APROBACION");

        assertFalse(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("modoAceptacion")
        ));
    }

    @Test
    void testModoAceptacionInvalido() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setModoAceptacion("AUTOMATICA");

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("modoAceptacion")
        ));
    }

    @Test
    void testPoliticaEsObligatoria() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setIdPolitica(null);

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("idPolitica")
        ));
    }

    @Test
    void testDebeSeleccionarAlMenosUnServicio() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setIdsServicios(List.of());

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("idsServicios")
        ));
    }

    @Test
    void testNoPuedeSeleccionarMasDeVeinteServicios() {
        EditarPublicacionRequest request = crearRequestValido();

        request.setIdsServicios(
            IntStream.range(0, 21)
                .mapToObj(i -> UUID.randomUUID())
                .toList()
        );

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("idsServicios")
        ));
    }

    @Test
    void testNoSePermitenServiciosNulos() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setIdsServicios(Arrays.asList((UUID) null));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().contains("idsServicios")
        ));
    }

    @Test
    void testImagenesSonOpcionales() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setImagenes(null);

        assertFalse(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("imagenes")
        ));
    }

    @Test
    void testListaDeImagenesVaciaEsValida() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setImagenes(List.of());

        assertFalse(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("imagenes")
        ));
    }

    @Test
    void testNoPuedeCargarMasDeDiezImagenes() {
        EditarPublicacionRequest request = crearRequestValido();

        List<MultipartFile> imagenes = IntStream.range(0, 11)
            .mapToObj(i -> (MultipartFile) new MockMultipartFile(
                "imagen" + i,
                "foto" + i + ".jpg",
                "image/jpeg",
                new byte[] {1, 2, 3}
            ))
            .toList();

        request.setImagenes(imagenes);

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("imagenes")
        ));
    }

    @Test
    void testNoSePermitenImagenesNulas() {
        EditarPublicacionRequest request = crearRequestValido();
        request.setImagenes(Arrays.asList((MultipartFile) null));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().contains("imagenes")
        ));
    }
}
