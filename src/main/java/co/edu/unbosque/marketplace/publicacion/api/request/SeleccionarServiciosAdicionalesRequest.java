package co.edu.unbosque.marketplace.publicacion.api.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SeleccionarServiciosAdicionalesRequest(

        @NotNull(message = "La lista de servicios adicionales es obligatoria.")
        List<@NotNull(message = "El identificador de cada servicio es obligatorio.") UUID> idsAdicionales
) {
}