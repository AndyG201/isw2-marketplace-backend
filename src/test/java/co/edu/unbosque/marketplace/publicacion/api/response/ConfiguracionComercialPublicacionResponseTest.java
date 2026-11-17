package co.edu.unbosque.marketplace.publicacion.api.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ConfiguracionComercialPublicacionResponseTest {

    @Test
    void testConstructorYGetters() {
        UUID id = UUID.randomUUID();
        BigDecimal tarifa = new BigDecimal("50000.00");
        List<ServicioAdicionalPublicacionResponse> servicios = new ArrayList<>();
        OffsetDateTime fecha = OffsetDateTime.now();

        ConfiguracionComercialPublicacionResponse response =
                new ConfiguracionComercialPublicacionResponse(
                        id, tarifa, servicios, fecha
                );

        assertEquals(id, response.getIdPublicacion());
        assertEquals(tarifa, response.getTarifaLimpieza());
        assertEquals(servicios, response.getServiciosAdicionales());
        assertEquals(fecha, response.getFechaActualizacion());
    }

    @Test
    void testListaServiciosInmodificable() {
        ConfiguracionComercialPublicacionResponse response =
                new ConfiguracionComercialPublicacionResponse(
                        UUID.randomUUID(),
                        BigDecimal.ZERO,
                        new ArrayList<>(),
                        OffsetDateTime.now()
                );

        assertThrows(UnsupportedOperationException.class, () ->
                response.getServiciosAdicionales().clear()
        );
    }

    @Test
    void testCopiaIndependienteDeLaLista() {
        List<ServicioAdicionalPublicacionResponse> servicios = new ArrayList<>();

        ConfiguracionComercialPublicacionResponse response =
                new ConfiguracionComercialPublicacionResponse(
                        UUID.randomUUID(),
                        new BigDecimal("30000.00"),
                        servicios,
                        OffsetDateTime.now()
                );

        servicios.clear();

        assertTrue(response.getServiciosAdicionales().isEmpty());
    }
}
