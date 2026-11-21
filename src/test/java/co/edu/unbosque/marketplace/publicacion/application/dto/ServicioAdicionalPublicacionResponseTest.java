package co.edu.unbosque.marketplace.publicacion.application.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ServicioAdicionalPublicacionResponseTest {

    @Test
    void debeCrearResponseCorrectamente() {
        UUID idAdicional = UUID.randomUUID();

        ServicioAdicionalPublicacionResponse response =
                new ServicioAdicionalPublicacionResponse(
                        idAdicional,
                        "Desayuno",
                        "Desayuno incluido durante la estadía",
                        new BigDecimal("25000.00"),
                        true
                );

        assertEquals(idAdicional, response.getIdAdicional());
        assertEquals("Desayuno", response.getNombre());
        assertEquals("Desayuno incluido durante la estadía",
                response.getDescripcion());
        assertEquals(new BigDecimal("25000.00"), response.getCosto());
        assertTrue(response.isActivo());
    }

    @Test
    void debeCrearResponseConServicioInactivo() {
        ServicioAdicionalPublicacionResponse response =
                new ServicioAdicionalPublicacionResponse(
                        UUID.randomUUID(),
                        "Transporte",
                        "Servicio de transporte adicional",
                        new BigDecimal("50000"),
                        false
                );

        assertNotNull(response.getIdAdicional());
        assertEquals("Transporte", response.getNombre());
        assertEquals("Servicio de transporte adicional",
                response.getDescripcion());
        assertEquals(new BigDecimal("50000"), response.getCosto());
        assertFalse(response.isActivo());
    }

    @Test
    void debePermitirCostoCero() {
        ServicioAdicionalPublicacionResponse response =
                new ServicioAdicionalPublicacionResponse(
                        UUID.randomUUID(),
                        "Parqueadero",
                        "Parqueadero gratuito",
                        BigDecimal.ZERO,
                        true
                );

        assertEquals(BigDecimal.ZERO, response.getCosto());
        assertTrue(response.isActivo());
    }

    @Test
    void debePermitirAtributosNulos() {
        ServicioAdicionalPublicacionResponse response =
                new ServicioAdicionalPublicacionResponse(
                        null,
                        null,
                        null,
                        null,
                        false
                );

        assertNull(response.getIdAdicional());
        assertNull(response.getNombre());
        assertNull(response.getDescripcion());
        assertNull(response.getCosto());
        assertFalse(response.isActivo());
    }
}
