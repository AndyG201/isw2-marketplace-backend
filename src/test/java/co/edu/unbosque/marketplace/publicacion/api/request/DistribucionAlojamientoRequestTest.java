package co.edu.unbosque.marketplace.publicacion.api.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class DistribucionAlojamientoRequestTest {

    private static final Set<String> CAMPOS = Set.of("habitaciones", "camas", "banos");

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void iniciar() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void cerrar() {
        factory.close();
    }

    static Stream<Supplier<DistribucionAlojamientoRequest>> solicitudes() {
        return Stream.of(CrearPublicacionRequest::new, EditarPublicacionRequest::new);
    }

    private static Set<String> erroresDeDistribucion(DistribucionAlojamientoRequest solicitud) {
        return validator.validate(solicitud).stream()
                .map(error -> error.getPropertyPath().toString())
                .filter(CAMPOS::contains)
                .collect(Collectors.toSet());
    }

    @ParameterizedTest
    @MethodSource("solicitudes")
    void aceptaLaDistribucionDentroDeLosLimitesOSinIndicarla(
            Supplier<DistribucionAlojamientoRequest> nueva) {

        DistribucionAlojamientoRequest solicitud = nueva.get();
        assertTrue(erroresDeDistribucion(solicitud).isEmpty());

        solicitud.setHabitaciones(3);
        solicitud.setCamas(4);
        solicitud.setBanos(2);

        assertTrue(erroresDeDistribucion(solicitud).isEmpty());
        assertEquals(3, solicitud.getHabitaciones());
        assertEquals(4, solicitud.getCamas());
        assertEquals(2, solicitud.getBanos());
    }

    @ParameterizedTest
    @MethodSource("solicitudes")
    void rechazaDistribucionesFueraDeLosLimites(
            Supplier<DistribucionAlojamientoRequest> nueva) {

        DistribucionAlojamientoRequest solicitud = nueva.get();
        solicitud.setHabitaciones(0);
        solicitud.setCamas(51);
        solicitud.setBanos(-1);

        assertEquals(CAMPOS, erroresDeDistribucion(solicitud));
    }
}
