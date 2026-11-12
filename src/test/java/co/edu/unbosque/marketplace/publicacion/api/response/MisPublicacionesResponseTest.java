package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class MisPublicacionesResponseTest {

    @Test
    void testConstructorYGetters() {
        List<PublicacionResumenResponse> publicaciones = List.of();

        MisPublicacionesResponse response =
            new MisPublicacionesResponse(
                "Publicaciones consultadas correctamente.",
                publicaciones
            );

        assertEquals(
            "Publicaciones consultadas correctamente.",
            response.getMensaje()
        );
        assertNotNull(response.getPublicaciones());
        assertEquals(publicaciones, response.getPublicaciones());
    }

    @Test
    void testListaDePublicacionesVacia() {
        MisPublicacionesResponse response =
            new MisPublicacionesResponse(
                "No tienes publicaciones registradas.",
                List.of()
            );

        assertTrue(response.getPublicaciones().isEmpty());
    }
}
