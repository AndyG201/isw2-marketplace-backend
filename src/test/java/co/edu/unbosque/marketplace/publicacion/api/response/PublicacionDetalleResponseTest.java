package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.servicio.api.response.ServicioResponse;

class PublicacionDetalleResponseTest {

    @Test
    void testConstructorYGetters() {
        UUID idPublicacion = UUID.randomUUID();
        UUID idAlojamiento = UUID.randomUUID();
        UUID idPolitica = UUID.randomUUID();

        OffsetDateTime fechaCreacion =
            OffsetDateTime.parse("2026-11-11T10:00:00-05:00");

        OffsetDateTime fechaActualizacion =
            OffsetDateTime.parse("2026-11-11T11:00:00-05:00");

        List<ServicioResponse> servicios = List.of();
        List<ImagenPublicacionResponse> imagenes = List.of();

        PublicacionDetalleResponse response =
            new PublicacionDetalleResponse(
                idPublicacion,
                idAlojamiento,
                idPolitica,
                "Apartamento en Bogotá",
                "Apartamento cómodo y equipado.",
                "No se permiten mascotas.",
                new BigDecimal("150000.00"),
                new BigDecimal("30000.00"),
                "INMEDIATA",
                "PUBLICADA",
                fechaCreacion,
                fechaActualizacion,
                "Carrera 15 # 80-20",
                "Bogotá",
                "APARTAMENTO",
                4,
                servicios,
                imagenes
            );

        assertEquals(idPublicacion, response.getIdPublicacion());
        assertEquals(idAlojamiento, response.getIdAlojamiento());
        assertEquals(idPolitica, response.getIdPolitica());
        assertEquals("Apartamento en Bogotá", response.getTitulo());
        assertEquals("Apartamento cómodo y equipado.", response.getDescripcion());
        assertEquals("No se permiten mascotas.", response.getReglas());
        assertEquals(new BigDecimal("150000.00"), response.getPrecioPorNoche());
        assertEquals(new BigDecimal("30000.00"), response.getTarifaLimpieza());
        assertEquals("INMEDIATA", response.getModoAceptacion());
        assertEquals("PUBLICADA", response.getEstado());
        assertEquals(fechaCreacion, response.getFechaCreacion());
        assertEquals(fechaActualizacion, response.getFechaActualizacion());
        assertEquals("Carrera 15 # 80-20", response.getUbicacion());
        assertEquals("Bogotá", response.getCiudad());
        assertEquals("APARTAMENTO", response.getTipoAlojamiento());
        assertEquals(4, response.getCapacidad());
        assertEquals(servicios, response.getServicios());
        assertEquals(imagenes, response.getImagenes());
    }

    @Test
    void testListasVacias() {
        PublicacionDetalleResponse response =
            new PublicacionDetalleResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Apartamento",
                "Descripción",
                "Reglas",
                new BigDecimal("100000"),
                BigDecimal.ZERO,
                "INMEDIATA",
                "BORRADOR",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "Calle 10",
                "Bogotá",
                "APARTAMENTO",
                2,
                List.of(),
                List.of()
            );

        assertTrue(response.getServicios().isEmpty());
        assertTrue(response.getImagenes().isEmpty());
    }
}
