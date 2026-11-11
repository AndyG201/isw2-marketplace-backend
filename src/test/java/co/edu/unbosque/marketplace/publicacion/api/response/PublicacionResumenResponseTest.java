package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PublicacionResumenResponseTest {

    @Test
    void testConstructorYGetters() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();

        OffsetDateTime fechaCreacion =
            OffsetDateTime.parse("2026-11-11T10:30:00-05:00");

        PublicacionResumenResponse response =
            new PublicacionResumenResponse(
                idPublicacion,
                idAlojamiento,
                "Apartamento en Bogotá",
                "Bogotá",
                "APARTAMENTO",
                4,
                new BigDecimal("150000.00"),
                "PUBLICADA",
                fechaCreacion
            );

        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals(idAlojamiento, response.getIdAlojamiento());
        assertEquals("Apartamento en Bogotá", response.getTitulo());
        assertEquals("Bogotá", response.getCiudad());
        assertEquals("APARTAMENTO", response.getTipoAlojamiento());
        assertEquals(4, response.getCapacidad());
        assertEquals(new BigDecimal("150000.00"), response.getPrecioPorNoche());
        assertEquals("PUBLICADA", response.getEstado());
        assertEquals(fechaCreacion, response.getFechaCreacion());
    }
}
