package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EstadoPublicacionResponseTest {

    private UUID idPublicacion;
    private String estadoAnterior;
    private String estadoActual;
    private String mensaje;
    private OffsetDateTime fechaActualizacion;
    private EstadoPublicacionResponse response;

    @BeforeEach
    void configurarDatos() {
        idPublicacion = UUID.randomUUID();
        estadoAnterior = "BORRADOR";
        estadoActual = "PUBLICADA";
        mensaje = "El estado de la publicación fue actualizado correctamente.";
        fechaActualizacion = OffsetDateTime.parse(
            "2026-11-11T10:30:00-05:00"
        );

        response = new EstadoPublicacionResponse(
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
        assertEquals("PUBLICADA", response.getEstadoActual());
    }

    @Test
    void testGetMensaje() {
        assertEquals(
            "El estado de la publicación fue actualizado correctamente.",
            response.getMensaje()
        );
    }

    @Test
    void testConstructorConValoresNulos() {
        EstadoPublicacionResponse responseNulo =
            new EstadoPublicacionResponse(null, null, null, null, null);

        assertNull(responseNulo.getIdPublicacion());
        assertNull(responseNulo.getEstadoAnterior());
        assertNull(responseNulo.getEstadoActual());
        assertNull(responseNulo.getMensaje());
    }
}
