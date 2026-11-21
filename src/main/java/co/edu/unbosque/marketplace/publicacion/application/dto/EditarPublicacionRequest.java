
package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EditarPublicacionRequest extends DatosPublicacionRequest {

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que cero.")
    private BigDecimal precioPorNoche;

    @DecimalMin(value = "0.00", message = "La tarifa de limpieza no puede ser negativa.")
    private BigDecimal tarifaLimpieza = BigDecimal.ZERO;

    @NotBlank(message = "El modo de aceptación es obligatorio.")
    @Pattern(
        regexp = "INMEDIATA|APROBACION",
        message = "El modo de aceptación debe ser INMEDIATA o APROBACION."
    )
    private String modoAceptacion;

    @Size(max = 10, message = "No puedes cargar más de 10 imágenes.")
    private List<@NotNull MultipartFile> imagenes;

    @Size(max = 1000, message = "El mensaje para el administrador no puede superar 1000 caracteres.")
    private String mensajeCambios;

    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public BigDecimal getTarifaLimpieza() { return tarifaLimpieza; }
    public void setTarifaLimpieza(BigDecimal tarifaLimpieza) { this.tarifaLimpieza = tarifaLimpieza; }

    public String getModoAceptacion() { return modoAceptacion; }
    public void setModoAceptacion(String modoAceptacion) { this.modoAceptacion = modoAceptacion; }

    public List<MultipartFile> getImagenes() { return imagenes; }

    public String getMensajeCambios() { return mensajeCambios; }

    public void setMensajeCambios(String mensajeCambios) { this.mensajeCambios = mensajeCambios; }
    public void setImagenes(List<MultipartFile> imagenes) { this.imagenes = imagenes; }
}
