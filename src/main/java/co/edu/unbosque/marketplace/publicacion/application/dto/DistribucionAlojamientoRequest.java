package co.edu.unbosque.marketplace.publicacion.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

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

    @Size(max = 120, message = "El nombre del propietario del inmueble no puede superar 120 caracteres.")
    private String propietarioInmueble;

    @Size(max = 120, message = "El país no puede superar 120 caracteres.")
    private String pais;

    @Size(max = 2, message = "El código del país debe tener 2 letras.")
    private String codigoPais;

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

    public String getPropietarioInmueble() {
        return propietarioInmueble;
    }

    public void setPropietarioInmueble(String propietarioInmueble) {
        this.propietarioInmueble = propietarioInmueble;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCodigoPais() {
        return codigoPais;
    }

    public void setCodigoPais(String codigoPais) {
        this.codigoPais = codigoPais;
    }
}
