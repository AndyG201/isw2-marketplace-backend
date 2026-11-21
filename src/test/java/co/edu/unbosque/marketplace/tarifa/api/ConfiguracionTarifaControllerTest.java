package co.edu.unbosque.marketplace.tarifa.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.tarifa.application.ConfiguracionTarifaService;
import co.edu.unbosque.marketplace.tarifa.application.dto.ConfiguracionTarifaResponse;
import co.edu.unbosque.marketplace.tarifa.application.dto.ConfigurarPorcentajesTarifaRequest;

class ConfiguracionTarifaControllerTest {

    private final ConfiguracionTarifaService service = mock(ConfiguracionTarifaService.class);
    private final ConfiguracionTarifaController controller = new ConfiguracionTarifaController(service);
    private final ConfiguracionTarifaResponse respuesta = new ConfiguracionTarifaResponse(
            UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00"), new BigDecimal("5.00"), null, null, null);

    @Test
    void consultaLaConfiguracionVigente() {
        when(service.consultarParaAdministracion()).thenReturn(respuesta);

        ResponseEntity<?> resultado = controller.consultarConfiguracionVigente();

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertSame(respuesta, resultado.getBody());
    }

    @Test
    void configuraLosPorcentajes() {
        UUID admin = UUID.randomUUID();
        ConfigurarPorcentajesTarifaRequest solicitud =
                new ConfigurarPorcentajesTarifaRequest(new BigDecimal("10.00"), new BigDecimal("5.00"));
        when(service.configurar(admin, solicitud)).thenReturn(respuesta);

        ResponseEntity<?> resultado = controller.configurar(admin, solicitud);

        assertEquals(HttpStatus.OK, resultado.getStatusCode());
        assertSame(respuesta, resultado.getBody());
    }

    @Test
    void sinAdministradorResponde401() {
        ResponseEntity<?> resultado = controller.configurar(null,
                new ConfigurarPorcentajesTarifaRequest(new BigDecimal("10.00"), new BigDecimal("5.00")));

        assertEquals(HttpStatus.UNAUTHORIZED, resultado.getStatusCode());
        verifyNoInteractions(service);
    }
}
