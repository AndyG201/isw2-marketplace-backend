package co.edu.unbosque.marketplace.publicacion.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ConfiguracionComercialPublicacionResponse {

    private UUID idPublicacion;
    private BigDecimal tarifaLimpieza;
    private List<ServicioAdicionalPublicacionResponse> serviciosAdicionales;
    private OffsetDateTime fechaActualizacion;

    public ConfiguracionComercialPublicacionResponse(
            UUID idPublicacion,
            BigDecimal tarifaLimpieza,
            List<ServicioAdicionalPublicacionResponse> serviciosAdicionales,
            OffsetDateTime fechaActualizacion) {

        this.idPublicacion = idPublicacion;
        this.tarifaLimpieza = tarifaLimpieza;
        this.serviciosAdicionales = List.copyOf(serviciosAdicionales);
        this.fechaActualizacion = fechaActualizacion;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public BigDecimal getTarifaLimpieza() {
        return tarifaLimpieza;
    }

    public List<ServicioAdicionalPublicacionResponse> getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
