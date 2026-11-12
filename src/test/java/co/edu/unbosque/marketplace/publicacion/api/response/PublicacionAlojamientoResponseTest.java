package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PublicacionAlojamientoResponseTest {

    @Test
    void testConstructorYGetters() {
        UUID idAlojamiento = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();

        List<String> imagenes = List.of(
            "imagenes/sala.jpg",
            "imagenes/habitacion.jpg"
        );

        PublicacionAlojamientoResponse response =
            new PublicacionAlojamientoResponse(
                idAlojamiento,
                idPublicacion,
                "Apartamento en Bogotá",
                "PUBLICADA",
                imagenes
            );

        assertEquals(idAlojamiento, response.getIdAlojamiento());
        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals("Apartamento en Bogotá", response.getTitulo());
        assertEquals("PUBLICADA", response.getEstado());
        assertEquals(imagenes, response.getImagenes());
    }

    @Test
    void testListaDeImagenesVacia() {
        PublicacionAlojamientoResponse response =
            new PublicacionAlojamientoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Apartamento",
                "BORRADOR",
                List.of()
            );

        assertTrue(response.getImagenes().isEmpty());
    }
}
