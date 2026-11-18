package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ServicioPublicacionResponseTest {

    @Test
    void debeCrearResponseCorrectamente() {
        UUID idServicio = UUID.randomUUID();

        ServicioPublicacionResponse response = new ServicioPublicacionResponse(
                idServicio,
                "Piscina",
                "Acceso a piscina climatizada"
        );

        assertEquals(idServicio, response.idServicio());
        assertEquals("Piscina", response.nombre());
        assertEquals("Acceso a piscina climatizada", response.descripcion());
    }

    @Test
    void debeCompararDosResponsesIguales() {
        UUID idServicio = UUID.randomUUID();

        ServicioPublicacionResponse response1 = new ServicioPublicacionResponse(
                idServicio, "Piscina", "Acceso a piscina climatizada"
        );

        ServicioPublicacionResponse response2 = new ServicioPublicacionResponse(
                idServicio, "Piscina", "Acceso a piscina climatizada"
        );

        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void debeDetectarResponsesDiferentes() {
        ServicioPublicacionResponse response1 = new ServicioPublicacionResponse(
                UUID.randomUUID(), "Piscina", "Acceso a piscina"
        );

        ServicioPublicacionResponse response2 = new ServicioPublicacionResponse(
                UUID.randomUUID(), "Gimnasio", "Acceso a gimnasio"
        );

        assertNotEquals(response1, response2);
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        ServicioPublicacionResponse response = new ServicioPublicacionResponse(
                UUID.randomUUID(),
                "Piscina",
                "Acceso a piscina climatizada"
        );

        String resultado = response.toString();

        assertNotNull(resultado);
        assertTrue(resultado.contains("Piscina"));
        assertTrue(resultado.contains("Acceso a piscina climatizada"));
    }
}
