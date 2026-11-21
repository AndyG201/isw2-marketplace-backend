package co.edu.unbosque.marketplace.cotizacion.api;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import co.edu.unbosque.marketplace.cotizacion.application.CotizacionService;
import co.edu.unbosque.marketplace.cotizacion.application.dto.CrearCotizacionRequest;

@RestController
@RequestMapping("/cotizaciones")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping
    public ResponseEntity<?> calcular(
            @Valid @RequestBody CrearCotizacionRequest solicitud,
            BindingResult resultadoValidacion) {

        if (resultadoValidacion.hasErrors()) {
            String mensaje = resultadoValidacion.getFieldErrors()
                    .stream()
                    .map(error -> error.getDefaultMessage())
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.joining(" "));

            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", mensaje));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cotizacionService.calcular(solicitud));
    }
}