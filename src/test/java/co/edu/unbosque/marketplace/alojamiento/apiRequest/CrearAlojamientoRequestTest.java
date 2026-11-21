package co.edu.unbosque.marketplace.alojamiento.apiRequest;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import co.edu.unbosque.marketplace.alojamiento.api.request.CrearAlojamientoRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class CrearAlojamientoRequestTest {

    private CrearAlojamientoRequest request;
    private Validator validator;
    private ValidatorFactory factory;

    @BeforeEach
    void setUp() {
        request = new CrearAlojamientoRequest();
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void debeAsignarYObtenerTodosLosAtributos() {
        UUID idPolitica = UUID.randomUUID();
        UUID idServicio = UUID.randomUUID();

        MultipartFile imagen = new MockMultipartFile(
                "imagen", "imagen.png", "image/png", new byte[]{1, 2, 3}
        );

        request.setTitulo("Apartamento moderno");
        request.setDescripcion("Apartamento completamente equipado.");
        request.setReglas("No fumar.");
        request.setUbicacion("Calle 123");
        request.setCiudad("Bogotá");
        request.setTipoAlojamiento("Apartamento");
        request.setCapacidad(4);
        request.setPrecioPorNoche(new BigDecimal("150000.00"));
        request.setTarifaLimpieza(new BigDecimal("25000.00"));
        request.setModoAceptacion("INMEDIATA");
        request.setIdPolitica(idPolitica);
        request.setIdsServicios(List.of(idServicio));
        request.setImagenes(List.of(imagen));

        assertEquals("Apartamento moderno", request.getTitulo());
        assertEquals("Apartamento completamente equipado.", request.getDescripcion());
        assertEquals("No fumar.", request.getReglas());
        assertEquals("Calle 123", request.getUbicacion());
        assertEquals("Bogotá", request.getCiudad());
        assertEquals("Apartamento", request.getTipoAlojamiento());
        assertEquals(4, request.getCapacidad());
        assertEquals(new BigDecimal("150000.00"), request.getPrecioPorNoche());
        assertEquals(new BigDecimal("25000.00"), request.getTarifaLimpieza());
        assertEquals("INMEDIATA", request.getModoAceptacion());
        assertEquals(idPolitica, request.getIdPolitica());
        assertEquals(List.of(idServicio), request.getIdsServicios());
        assertEquals(List.of(imagen), request.getImagenes());
    }

    @Test
    void debeInicializarTarifaLimpiezaEnCero() {
        assertEquals(BigDecimal.ZERO, request.getTarifaLimpieza());
    }

    @Test
    void debePermitirTarifaLimpiezaEnCero() {
        request.setTarifaLimpieza(BigDecimal.ZERO);

        assertEquals(BigDecimal.ZERO, request.getTarifaLimpieza());
    }

    @Test
    void debeDetectarCamposObligatoriosNulos() {
        Set<?> errores = validator.validate(request);

        assertFalse(errores.isEmpty());
        assertTrue(errores.size() >= 12);
    }

    @Test
    void debeAceptarUnaSolicitudValida() {
        completarSolicitudValida();

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void debeRechazarTituloVacio() {
        completarSolicitudValida();
        request.setTitulo(" ");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void debeRechazarTituloMayorA200Caracteres() {
        completarSolicitudValida();
        request.setTitulo("A".repeat(201));

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals("titulo")));
    }

    @Test
    void debeRechazarCiudadMayorA100Caracteres() {
        completarSolicitudValida();
        request.setCiudad("A".repeat(101));

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals("ciudad")));
    }

    @Test
    void debeRechazarCapacidadCero() {
        completarSolicitudValida();
        request.setCapacidad(0);

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals("capacidad")));
    }

    @Test
    void debeRechazarPrecioCero() {
        completarSolicitudValida();
        request.setPrecioPorNoche(BigDecimal.ZERO);

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals("precioPorNoche")));
    }

    @Test
    void debeRechazarTarifaLimpiezaNegativa() {
        completarSolicitudValida();
        request.setTarifaLimpieza(new BigDecimal("-1"));

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals("tarifaLimpieza")));
    }

    @Test
    void debeRechazarMasDe20Servicios() {
        completarSolicitudValida();
        request.setIdsServicios(
                java.util.stream.IntStream.range(0, 21)
                        .mapToObj(i -> UUID.randomUUID())
                        .toList()
        );

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals("idsServicios")));
    }

    @Test
    void debeRechazarMasDe10Imagenes() {
        completarSolicitudValida();

        List<MultipartFile> imagenes = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> (MultipartFile) new MockMultipartFile(
                        "imagen" + i, "imagen.png", "image/png", new byte[]{1}
                ))
                .toList();

        request.setImagenes(imagenes);

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().equals("imagenes")));
    }

    @Test
    void debeRechazarServiciosConIdentificadoresNulos() {
        completarSolicitudValida();
        request.setIdsServicios(java.util.Arrays.asList((UUID) null));

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().startsWith("idsServicios")));
    }

    @Test
    void debeRechazarImagenesNulasEnLaLista() {
        completarSolicitudValida();
        request.setImagenes(java.util.Arrays.asList((MultipartFile) null));

        assertTrue(validator.validate(request).stream()
                .anyMatch(error -> error.getPropertyPath().toString().startsWith("imagenes")));
    }

    private void completarSolicitudValida() {
        request.setTitulo("Apartamento moderno");
        request.setDescripcion("Apartamento completamente equipado.");
        request.setReglas("No fumar.");
        request.setUbicacion("Calle 123");
        request.setCiudad("Bogotá");
        request.setTipoAlojamiento("Apartamento");
        request.setCapacidad(4);
        request.setPrecioPorNoche(new BigDecimal("150000"));
        request.setTarifaLimpieza(BigDecimal.ZERO);
        request.setModoAceptacion("INMEDIATA");
        request.setIdPolitica(UUID.randomUUID());
        request.setIdsServicios(List.of(UUID.randomUUID()));
        request.setImagenes(List.of(
                new MockMultipartFile(
                        "imagen", "imagen.png", "image/png", new byte[]{1, 2, 3}
                )
        ));
    }
}