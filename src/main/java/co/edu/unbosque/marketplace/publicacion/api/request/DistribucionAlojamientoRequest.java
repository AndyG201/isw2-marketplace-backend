package co.edu.unbosque.marketplace.publicacion.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public abstract class DistribucionAlojamientoRequest {

    @Min(value = 1, message = "Debe haber al menos una habitación.")
    @Max(value = 50, message = "El número de habitaciones no puede superar 50.")
    private Integer habitaciones;

    @Min(value = 1, message = "Debe haber al menos una cama.")
    @Max(value = 50, message = "El número de camas no puede superar 50.")
    private Integer camas;

    @Min(value = 1, message = "Debe haber al menos un baño.")
    @Max(value = 50, message = "El número de baños no puede superar 50.")
    private Integer banos;

    public Integer getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(Integer habitaciones) {
        this.habitaciones = habitaciones;
    }

    public Integer getCamas() {
        return camas;
    }

    public void setCamas(Integer camas) {
        this.camas = camas;
    }

    public Integer getBanos() {
        return banos;
    }

    public void setBanos(Integer banos) {
        this.banos = banos;
    }
}
