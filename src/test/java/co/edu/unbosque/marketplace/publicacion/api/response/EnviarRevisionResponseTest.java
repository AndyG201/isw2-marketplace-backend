package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnviarRevisionResponseTest {

    private UUID idPublicacion;
    private String estadoAnterior;
    private String estadoActual;
    private String mensaje;
    private OffsetDateTime fechaActualizacion;

    private EnviarRevisionResponse response;

    @BeforeEach
    void configurarDatos() {
        idPublicacion = UUID.randomUUID();
        estadoAnterior = "BORRADOR";
        estadoActual = "PENDIENTE_REVISION";
        mensaje = "La publicación fue enviada a revisión correctamente.";
        fechaActualizacion = OffsetDateTime.parse(
            "2026-11-11T10:30:00-05:00"
        );

        response = new EnviarRevisionResponse(
            idPublicacion,
            estadoAnterior,
            estadoActual,
            mensaje,
            fechaActualizacion
        );
    }

    @Test
    void testConstructor() {
        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals(estadoAnterior, response.getEstadoAnterior());
        assertEquals(estadoActual, response.getEstadoActual());
        assertEquals(mensaje, response.getMensaje());
        assertEquals(fechaActualizacion, response.getFechaActualizacion());
    }

    @Test
    void testGetIdPublicacion() {
        assertEquals(idPublicacion, response.getIdPublicacion());
    }

    @Test
    void testGetEstadoAnterior() {
        assertEquals("BORRADOR", response.getEstadoAnterior());
    }

    @Test
    void testGetEstadoActual() {
        assertEquals("PENDIENTE_REVISION", response.getEstadoActual());
    }

    @Test
    void testGetMensaje() {
        assertEquals(
            "La publicación fue enviada a revisión correctamente.",
            response.getMensaje()
        );
    }

    @Test
    void testGetFechaActualizacion() {
        assertEquals(fechaActualizacion, response.getFechaActualizacion());
    }

    @Test
    void testConstructorConValoresNulos() {
        EnviarRevisionResponse responseNulo =
            new EnviarRevisionResponse(
                null,
                null,
                null,
                null,
                null
            );

        assertNull(responseNulo.getIdPublicacion());
        assertNull(responseNulo.getEstadoAnterior());
        assertNull(responseNulo.getEstadoActual());
        assertNull(responseNulo.getMensaje());
        assertNull(responseNulo.getFechaActualizacion());
    }
}
