package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public abstract class DatosPublicacionRequest extends DistribucionAlojamientoRequest {

    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 200)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria.")
    private String descripcion;

    @NotBlank(message = "Las reglas son obligatorias.")
    private String reglas;

    @NotBlank(message = "La ubicación es obligatoria.")
    @Size(max = 255)
    private String ubicacion;

    @NotBlank(message = "La ciudad es obligatoria.")
    @Size(max = 100)
    private String ciudad;

    @NotBlank(message = "El tipo de alojamiento es obligatorio.")
    @Size(max = 50)
    private String tipoAlojamiento;

    @NotNull(message = "La capacidad es obligatoria.")
    @Min(value = 1, message = "La capacidad debe ser mayor que cero.")
    private Integer capacidad;

    @NotNull(message = "Debes seleccionar una política.")
    private UUID idPolitica;

    @NotEmpty(message = "Debes seleccionar al menos un servicio.")
    @Size(max = 20)
    private List<@NotNull UUID> idsServicios;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getReglas() { return reglas; }
    public void setReglas(String reglas) { this.reglas = reglas; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getTipoAlojamiento() { return tipoAlojamiento; }
    public void setTipoAlojamiento(String tipoAlojamiento) { this.tipoAlojamiento = tipoAlojamiento; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public UUID getIdPolitica() { return idPolitica; }
    public void setIdPolitica(UUID idPolitica) { this.idPolitica = idPolitica; }

    public List<UUID> getIdsServicios() { return idsServicios; }
    public void setIdsServicios(List<UUID> idsServicios) { this.idsServicios = idsServicios; }
}
