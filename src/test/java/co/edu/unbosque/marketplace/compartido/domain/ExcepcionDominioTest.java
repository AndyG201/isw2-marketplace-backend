package co.edu.unbosque.marketplace.compartido.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ExcepcionDominioTest {

    static Stream<BiFunction<String, Throwable, ExcepcionDominio>> conCausa() {
        return Stream.of(
                DatosInvalidosException::new,
                SesionRequeridaException::new,
                AccesoNoPermitidoException::new,
                RecursoNoEncontradoException::new,
                ConflictoNegocioException::new,
                PlazoVencidoException::new,
                ServicioNoDisponibleException::new,
                FallaInternaException::new);
    }

    @ParameterizedTest
    @MethodSource("conCausa")
    void conservaElMensajeYLaCausaOriginal(BiFunction<String, Throwable, ExcepcionDominio> crear) {
        IllegalStateException causa = new IllegalStateException("origen");

        ExcepcionDominio error = crear.apply("Mensaje para el usuario.", causa);

        assertEquals("Mensaje para el usuario.", error.getMessage());
        assertSame(causa, error.getCause());
    }
}
