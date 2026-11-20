package co.edu.unbosque.marketplace.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

class ErroresApiHandlerTest {

    private final ErroresApiHandler manejador = new ErroresApiHandler();

    @ParameterizedTest
    @CsvSource(value = {
            "CONFLICT|El alojamiento no está disponible para las fechas indicadas.|El alojamiento no está disponible para las fechas indicadas.",
            "GONE|El plazo de pago de la reserva venció.|El plazo de pago de la reserva venció.",
            "NOT_FOUND|' '|No fue posible completar la solicitud.",
            "FORBIDDEN||No fue posible completar la solicitud."
    }, delimiter = '|')
    void conservaElEstadoYDevuelveElMotivoComoMensaje(HttpStatus estado, String motivo, String esperado) {
        ResponseEntity<Map<String, String>> respuesta =
                manejador.manejar(new ResponseStatusException(estado, motivo));

        assertEquals(estado, respuesta.getStatusCode());
        assertEquals(Map.of("mensaje", esperado), respuesta.getBody());
    }
}
