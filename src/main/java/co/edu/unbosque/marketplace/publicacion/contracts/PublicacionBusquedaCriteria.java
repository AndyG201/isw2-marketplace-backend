package co.edu.unbosque.marketplace.publicacion.contracts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PublicacionBusquedaCriteria(
        String ciudad,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        Integer capacidad,
        String tipoAlojamiento,
        BigDecimal precioMinimo,
        BigDecimal precioMaximo,
        List<UUID> servicios,
        int pagina,
        int tamano) {

    public PublicacionBusquedaCriteria {
        servicios = servicios == null ? List.of() : List.copyOf(servicios);
    }
}