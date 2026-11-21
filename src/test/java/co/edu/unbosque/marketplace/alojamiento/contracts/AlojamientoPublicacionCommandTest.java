package co.edu.unbosque.marketplace.alojamiento.contracts;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;

class AlojamientoPublicacionCommandTest {

    private AlojamientoPublicacionCommand comando;

    private UUID idAlojamiento;
    private UUID idAnfitrion;

    @BeforeEach
    void setUp() {
        comando = mock(AlojamientoPublicacionCommand.class);
        idAlojamiento = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();
    }

    @Test
    void debeCrearAlojamientoYRetornarIdentificador() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion,
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        when(comando.crearAlojamiento(solicitud))
                .thenReturn(idAlojamiento);

        UUID resultado = comando.crearAlojamiento(solicitud);

        assertNotNull(resultado);
        assertEquals(idAlojamiento, resultado);

        verify(comando).crearAlojamiento(solicitud);
    }

    @Test
    void debeActualizarAlojamiento() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4,
                        null,
                        null,
                        null
                );

        doNothing().when(comando)
                .actualizarAlojamiento(idAlojamiento, solicitud);

        assertDoesNotThrow(() ->
                comando.actualizarAlojamiento(idAlojamiento, solicitud)
        );

        verify(comando).actualizarAlojamiento(idAlojamiento, solicitud);
    }

    @Test
    void debePermitirRetornarNullEnUnaImplementacionSimulada() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion,
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        when(comando.crearAlojamiento(solicitud)).thenReturn(null);

        assertNull(comando.crearAlojamiento(solicitud));
    }

    @Test
    void debePropagarExcepcionAlActualizar() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4,
                        null,
                        null,
                        null
                );

        doThrow(new DatosInvalidosException("Solicitud inválida."))
                .when(comando)
                .actualizarAlojamiento(idAlojamiento, solicitud);

        DatosInvalidosException exception = assertThrows(
                DatosInvalidosException.class,
                () -> comando.actualizarAlojamiento(idAlojamiento, solicitud)
        );

        assertEquals("Solicitud inválida.", exception.getMessage());
    }
}
