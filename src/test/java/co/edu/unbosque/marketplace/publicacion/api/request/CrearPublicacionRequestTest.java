package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CrearPublicacionRequestTest {

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

    private CrearPublicacionRequest crearRequestValido() {
        CrearPublicacionRequest request = new CrearPublicacionRequest();

        request.setTitulo("Apartamento moderno");
        request.setDescripcion("Apartamento cómodo y bien ubicado.");
        request.setReglas("No fumar dentro del alojamiento.");
        request.setUbicacion("Calle 100 # 15-20");
        request.setCiudad("Bogotá");
        request.setTipoAlojamiento("Apartamento");
        request.setCapacidad(4);
        request.setPrecioPorNoche(new BigDecimal("150000"));
        request.setTarifaLimpieza(new BigDecimal("20000"));
        request.setModoAceptacion("AUTOMATICA");
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
        CrearPublicacionRequest request = crearRequestValido();

        Set<ConstraintViolation<CrearPublicacionRequest>> errores =
            validator.validate(request);

        assertTrue(errores.isEmpty());
    }

    @Test
    void testCamposObligatorios() {
        CrearPublicacionRequest request = new CrearPublicacionRequest();

        Set<ConstraintViolation<CrearPublicacionRequest>> errores =
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
        CrearPublicacionRequest request = crearRequestValido();
        request.setTitulo("A".repeat(201));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("titulo")
        ));
    }

    @Test
    void testUbicacionSuperaLongitudMaxima() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setUbicacion("A".repeat(256));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("ubicacion")
        ));
    }

    @Test
    void testCiudadSuperaLongitudMaxima() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setCiudad("A".repeat(101));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("ciudad")
        ));
    }

    @Test
    void testTipoAlojamientoSuperaLongitudMaxima() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setTipoAlojamiento("A".repeat(51));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("tipoAlojamiento")
        ));
    }

    @Test
    void testCapacidadDebeSerMayorQueCero() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setCapacidad(0);

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("capacidad")
        ));
    }

    @Test
    void testPrecioPorNocheDebeSerMayorQueCero() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setPrecioPorNoche(BigDecimal.ZERO);

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("precioPorNoche")
        ));
    }

    @Test
    void testTarifaLimpiezaNoPuedeSerNegativa() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setTarifaLimpieza(new BigDecimal("-1"));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("tarifaLimpieza")
        ));
    }

    @Test
    void testTarifaLimpiezaPuedeSerCero() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setTarifaLimpieza(BigDecimal.ZERO);

        assertFalse(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("tarifaLimpieza")
        ));
    }

    @Test
    void testModoAceptacionSuperaLongitudMaxima() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setModoAceptacion("A".repeat(31));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("modoAceptacion")
        ));
    }

    @Test
    void testPoliticaEsObligatoria() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setIdPolitica(null);

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("idPolitica")
        ));
    }

    @Test
    void testDebeSeleccionarAlMenosUnServicio() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setIdsServicios(List.of());

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("idsServicios")
        ));
    }

    @Test
    void testNoPuedeSeleccionarMasDeVeinteServicios() {
        CrearPublicacionRequest request = crearRequestValido();

        request.setIdsServicios(
            java.util.stream.IntStream.range(0, 21)
                .mapToObj(i -> UUID.randomUUID())
                .toList()
        );

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("idsServicios")
        ));
    }

    @Test
    void testNoSePermitenServiciosNulos() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setIdsServicios(java.util.Arrays.asList((UUID) null));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().contains("idsServicios")
        ));
    }

    @Test
    void testDebeCargarAlMenosUnaImagen() {
        CrearPublicacionRequest request = crearRequestValido();
        request.setImagenes(List.of());

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().equals("imagenes")
        ));
    }

    @Test
    void testNoPuedeCargarMasDeDiezImagenes() {
        CrearPublicacionRequest request = crearRequestValido();

        List<MultipartFile> imagenes =
            java.util.stream.IntStream.range(0, 11)
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
        CrearPublicacionRequest request = crearRequestValido();
        request.setImagenes(java.util.Arrays.asList((MultipartFile) null));

        assertTrue(validator.validate(request).stream().anyMatch(
            error -> error.getPropertyPath().toString().contains("imagenes")
        ));
    }
}
