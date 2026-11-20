package co.edu.unbosque.marketplace.config;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErroresApiHandler {

    private static final String MENSAJE_GENERICO = "No fue posible completar la solicitud.";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> manejar(ResponseStatusException error) {
        String motivo = error.getReason();
        String mensaje = motivo == null || motivo.isBlank() ? MENSAJE_GENERICO : motivo;
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("mensaje", mensaje));
    }
}
