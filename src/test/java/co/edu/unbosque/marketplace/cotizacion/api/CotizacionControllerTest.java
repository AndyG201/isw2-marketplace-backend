package co.edu.unbosque.marketplace.cotizacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import co.edu.unbosque.marketplace.cotizacion.application.CotizacionService;
import co.edu.unbosque.marketplace.cotizacion.application.dto.CotizacionResponse;
import co.edu.unbosque.marketplace.cotizacion.application.dto.CrearCotizacionRequest;

class CotizacionControllerTest {

    private final CotizacionService service = mock(CotizacionService.class);
    private final CotizacionController controller = new CotizacionController(service);
    private final CrearCotizacionRequest solicitud = new CrearCotizacionRequest(
            UUID.randomUUID(), LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 12), 2, null);

    @Test
    void crea201CuandoLaSolicitudEsValida() {
        CotizacionResponse respuesta = mock(CotizacionResponse.class);
        when(service.calcular(solicitud)).thenReturn(respuesta);

        ResponseEntity<?> resultado = controller.calcular(solicitud, new BeanPropertyBindingResult(solicitud, "solicitud"));

        assertEquals(HttpStatus.CREATED, resultado.getStatusCode());
        assertSame(respuesta, resultado.getBody());
    }

    @Test
    void responde400ConLosMensajesDeValidacion() {
        BindingResult errores = new BeanPropertyBindingResult(solicitud, "solicitud");
        errores.addError(new FieldError("solicitud", "fechaEntrada", "La fecha de entrada es obligatoria."));

        ResponseEntity<?> resultado = controller.calcular(solicitud, errores);

        assertEquals(HttpStatus.BAD_REQUEST, resultado.getStatusCode());
        verifyNoInteractions(service);
    }
}
