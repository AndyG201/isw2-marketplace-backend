package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DecisionPublicacionResponseTest {

    private UUID idRevision;
    private UUID idPublicacion;
    private String accion;
    private String estadoAnterior;
    private String estadoActual;
    private String motivo;
    private String mensaje;
    private OffsetDateTime fechaRevision;

    private DecisionPublicacionResponse response;

    @BeforeEach
    void configurarDatos() {
        idRevision = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        accion = "APROBAR";
        estadoAnterior = "PENDIENTE_REVISION";
        estadoActual = "PUBLICADA";
        motivo = "La publicación cumple con los requisitos.";
        mensaje = "Publicación aprobada correctamente.";
        fechaRevision = OffsetDateTime.parse("2026-11-11T10:30:00-05:00");

        response = new DecisionPublicacionResponse(
            idRevision,
            idPublicacion,
            accion,
            estadoAnterior,
            estadoActual,
            motivo,
            mensaje,
            fechaRevision
        );
    }

    @Test
    void testConstructor() {
        assertEquals(idRevision, response.getIdRevision());
        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals(accion, response.getAccion());
        assertEquals(estadoAnterior, response.getEstadoAnterior());
        assertEquals(estadoActual, response.getEstadoActual());
        assertEquals(motivo, response.getMotivo());
        assertEquals(mensaje, response.getMensaje());
        assertEquals(fechaRevision, response.getFechaRevision());
    }

    @Test
    void testGetIdRevision() {
        assertEquals(idRevision, response.getIdRevision());
    }

    @Test
    void testGetIdPublicacion() {
        assertEquals(idPublicacion, response.getIdPublicacion());
    }

    @Test
    void testGetAccion() {
        assertEquals("APROBAR", response.getAccion());
    }

    @Test
    void testGetEstadoAnterior() {
        assertEquals("PENDIENTE_REVISION", response.getEstadoAnterior());
    }

    @Test
    void testGetEstadoActual() {
        assertEquals("PUBLICADA", response.getEstadoActual());
    }

    @Test
    void testGetMotivo() {
        assertEquals(motivo, response.getMotivo());
    }

    @Test
    void testGetMensaje() {
        assertEquals(mensaje, response.getMensaje());
    }

    @Test
    void testGetFechaRevision() {
        assertEquals(fechaRevision, response.getFechaRevision());
    }

    @Test
    void testConstructorConValoresNulos() {
        DecisionPublicacionResponse responseNulo =
            new DecisionPublicacionResponse(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

        assertNull(responseNulo.getIdRevision());
        assertNull(responseNulo.getIdPublicacion());
        assertNull(responseNulo.getAccion());
        assertNull(responseNulo.getEstadoAnterior());
        assertNull(responseNulo.getEstadoActual());
        assertNull(responseNulo.getMotivo());
        assertNull(responseNulo.getMensaje());
        assertNull(responseNulo.getFechaRevision());
    }
}
