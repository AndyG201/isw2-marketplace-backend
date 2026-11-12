package co.edu.unbosque.marketplace.publicacion.api.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ServicioAdicionalRequest {

    @NotBlank(message = "El nombre del servicio es obligatorio.")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El costo del servicio es obligatorio.")
    @DecimalMin(
            value = "0.00",
            message = "El costo del servicio no puede ser negativo."
    )
    private BigDecimal costo;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
}
