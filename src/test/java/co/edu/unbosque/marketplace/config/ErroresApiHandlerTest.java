package co.edu.unbosque.marketplace.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.ExcepcionDominio;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.compartido.domain.PlazoVencidoException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;
import co.edu.unbosque.marketplace.compartido.domain.ServicioNoDisponibleException;
import co.edu.unbosque.marketplace.compartido.domain.SesionRequeridaException;

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

    static Stream<Arguments> erroresDeDominio() {
        return Stream.of(
                Arguments.of(new DatosInvalidosException("Las fechas de la cotización no son válidas."),
                        HttpStatus.BAD_REQUEST),
                Arguments.of(new SesionRequeridaException("Debes iniciar sesión."), HttpStatus.UNAUTHORIZED),
                Arguments.of(new AccesoNoPermitidoException("La reserva pertenece a otro huésped."),
                        HttpStatus.FORBIDDEN),
                Arguments.of(new RecursoNoEncontradoException("La reserva no existe."), HttpStatus.NOT_FOUND),
                Arguments.of(new ConflictoNegocioException("La reserva no está pendiente de pago."),
                        HttpStatus.CONFLICT),
                Arguments.of(new PlazoVencidoException("El plazo de pago de la reserva venció."), HttpStatus.GONE),
                Arguments.of(new ServicioNoDisponibleException("El almacenamiento no está habilitado."),
                        HttpStatus.SERVICE_UNAVAILABLE),
                Arguments.of(new FallaInternaException("No fue posible leer el archivo exportado."),
                        HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ParameterizedTest
    @MethodSource("erroresDeDominio")
    void traduceCadaErrorDeDominioASuEstadoHttpConElMismoMensaje(ExcepcionDominio error, HttpStatus estado) {
        ResponseEntity<Map<String, String>> respuesta = manejador.manejar(error);

        assertEquals(estado, respuesta.getStatusCode());
        assertEquals(Map.of("mensaje", error.getMessage()), respuesta.getBody());
    }

    @Test
    void usaElMensajeGenericoSiElErrorDeDominioNoTraeMensaje() {
        ResponseEntity<Map<String, String>> respuesta = manejador.manejar(new ConflictoNegocioException(" "));

        assertEquals(HttpStatus.CONFLICT, respuesta.getStatusCode());
        assertEquals(Map.of("mensaje", "No fue posible completar la solicitud."), respuesta.getBody());
    }
}
