package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PublicacionAdministrativaResponseTest {

    @Test
    void testConstructorYGetters() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();

        OffsetDateTime fechaCreacion = OffsetDateTime.now();
        OffsetDateTime fechaActualizacion = fechaCreacion.plusDays(1);

        PublicacionAdministrativaResponse response =
                new PublicacionAdministrativaResponse(
                        idPublicacion,
                        idAlojamiento,
                        "Apartamento en Bogotá",
                        "PUBLICADA",
                        fechaCreacion,
                        fechaActualizacion
                );

        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals(idAlojamiento, response.getIdAlojamiento());
        assertEquals("Apartamento en Bogotá", response.getTitulo());
        assertEquals("PUBLICADA", response.getEstado());
        assertEquals(fechaCreacion, response.getFechaCreacion());
        assertEquals(fechaActualizacion, response.getFechaActualizacion());
    }

    @Test
    void testValoresNulos() {
        PublicacionAdministrativaResponse response =
                new PublicacionAdministrativaResponse(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );

        assertEquals(null, response.getIdPublicacion());
        assertEquals(null, response.getIdAlojamiento());
        assertEquals(null, response.getTitulo());
        assertEquals(null, response.getEstado());
        assertEquals(null, response.getFechaCreacion());
        assertEquals(null, response.getFechaActualizacion());
    }
}
