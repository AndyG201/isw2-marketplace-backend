package co.edu.unbosque.marketplace.publicacion.api.request;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class ConfigurarConfiguracionComercialRequest {

    @NotNull(message = "La tarifa de limpieza es obligatoria.")
    @DecimalMin(
            value = "0.00",
            message = "La tarifa de limpieza no puede ser negativa."
    )
    private BigDecimal tarifaLimpieza;

    @NotNull(message = "La lista de servicios adicionales es obligatoria.")
    private List<@Valid ServicioAdicionalRequest> serviciosAdicionales;

    public BigDecimal getTarifaLimpieza() {
        return tarifaLimpieza;
    }

    public void setTarifaLimpieza(BigDecimal tarifaLimpieza) {
        this.tarifaLimpieza = tarifaLimpieza;
    }

    public List<ServicioAdicionalRequest> getServiciosAdicionales() {
        return serviciosAdicionales;
    }

    public void setServiciosAdicionales(
            List<ServicioAdicionalRequest> serviciosAdicionales) {

        this.serviciosAdicionales = serviciosAdicionales;
    }
}
