
package co.edu.unbosque.marketplace.alojamiento.api.request;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearAlojamientoRequest {

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

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01")
    private BigDecimal precioPorNoche;

    @DecimalMin(value = "0.00")
    private BigDecimal tarifaLimpieza = BigDecimal.ZERO;

    @NotBlank(message = "El modo de aceptación es obligatorio.")
    @Size(max = 30)
    private String modoAceptacion;

    @NotNull(message = "Debes seleccionar una política.")
    private UUID idPolitica;

    @NotEmpty(message = "Debes seleccionar al menos un servicio.")
    @Size(max = 20)
    private List<@NotNull UUID> idsServicios;

    @NotEmpty(message = "Debes cargar al menos una imagen.")
    @Size(max = 10)
    private List<@NotNull MultipartFile> imagenes;

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

    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public BigDecimal getTarifaLimpieza() { return tarifaLimpieza; }
    public void setTarifaLimpieza(BigDecimal tarifaLimpieza) { this.tarifaLimpieza = tarifaLimpieza; }

    public String getModoAceptacion() { return modoAceptacion; }
    public void setModoAceptacion(String modoAceptacion) { this.modoAceptacion = modoAceptacion; }

    public UUID getIdPolitica() { return idPolitica; }
    public void setIdPolitica(UUID idPolitica) { this.idPolitica = idPolitica; }

    public List<UUID> getIdsServicios() { return idsServicios; }
    public void setIdsServicios(List<UUID> idsServicios) { this.idsServicios = idsServicios; }

    public List<MultipartFile> getImagenes() { return imagenes; }
    public void setImagenes(List<MultipartFile> imagenes) { this.imagenes = imagenes; }
}
