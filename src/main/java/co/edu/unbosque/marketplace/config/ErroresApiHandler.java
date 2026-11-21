package co.edu.unbosque.marketplace.config;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
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

@RestControllerAdvice
public class ErroresApiHandler {

    private static final String MENSAJE_GENERICO = "No fue posible completar la solicitud.";

    @ExceptionHandler(ExcepcionDominio.class)
    public ResponseEntity<Map<String, String>> manejar(ExcepcionDominio error) {
        return respuesta(estado(error), error.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejar(ResponseStatusException error) {
        return respuesta(error.getStatusCode(), error.getReason());
    }

    static HttpStatus estado(ExcepcionDominio error) {
        return switch (error) {
            case DatosInvalidosException ignorada -> HttpStatus.BAD_REQUEST;
            case SesionRequeridaException ignorada -> HttpStatus.UNAUTHORIZED;
            case AccesoNoPermitidoException ignorada -> HttpStatus.FORBIDDEN;
            case RecursoNoEncontradoException ignorada -> HttpStatus.NOT_FOUND;
            case ConflictoNegocioException ignorada -> HttpStatus.CONFLICT;
            case PlazoVencidoException ignorada -> HttpStatus.GONE;
            case ServicioNoDisponibleException ignorada -> HttpStatus.SERVICE_UNAVAILABLE;
            case FallaInternaException ignorada -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private ResponseEntity<Map<String, String>> respuesta(HttpStatusCode estado, String motivo) {
        String mensaje = motivo == null || motivo.isBlank() ? MENSAJE_GENERICO : motivo;
        return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
    }
}
