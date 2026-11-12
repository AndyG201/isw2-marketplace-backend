package co.edu.unbosque.marketplace.publicacion.api.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public class SeleccionarPoliticaCancelacionRequest {

    @NotNull(message = "Debes seleccionar una política de cancelación.")
    private UUID idPolitica;

    public UUID getIdPolitica() {
        return idPolitica;
    }

    public void setIdPolitica(UUID idPolitica) {
        this.idPolitica = idPolitica;
    }
}
