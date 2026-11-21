package co.edu.unbosque.marketplace.resena.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ResenasPublicacionResponse(
        UUID idPublicacion,
        List<ResenaPublicacionItemResponse> resenas,
        int totalResenas,
        BigDecimal calificacionPromedio,
        String mensaje) {
}
