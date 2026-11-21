
package co.edu.unbosque.marketplace.publicacion.application.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CrearPublicacionRequest extends DatosPublicacionRequest {

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01")
    private BigDecimal precioPorNoche;

    @DecimalMin(value = "0.00")
    private BigDecimal tarifaLimpieza = BigDecimal.ZERO;

    @NotBlank(message = "El modo de aceptación es obligatorio.")
    @Pattern(
            regexp = "INMEDIATA|APROBACION",
            message = "El modo debe ser INMEDIATA o APROBACION."
    )
    @Size(max = 30)
    private String modoAceptacion;

    @NotEmpty(message = "Debes cargar al menos una imagen.")
    @Size(max = 10)
    private List<@NotNull MultipartFile> imagenes;

    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public void setPrecioPorNoche(BigDecimal precioPorNoche) { this.precioPorNoche = precioPorNoche; }

    public BigDecimal getTarifaLimpieza() { return tarifaLimpieza; }
    public void setTarifaLimpieza(BigDecimal tarifaLimpieza) { this.tarifaLimpieza = tarifaLimpieza; }

    public String getModoAceptacion() { return modoAceptacion; }
    public void setModoAceptacion(String modoAceptacion) { this.modoAceptacion = modoAceptacion; }

    public List<MultipartFile> getImagenes() { return imagenes; }
    public void setImagenes(List<MultipartFile> imagenes) { this.imagenes = imagenes; }
}
