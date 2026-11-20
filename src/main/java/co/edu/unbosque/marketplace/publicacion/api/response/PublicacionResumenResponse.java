package co.edu.unbosque.marketplace.publicacion.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicacionResumenResponse {

    private UUID idPublicacion;
    private UUID idAlojamiento;
    private String titulo;
    private String ciudad;
    private String tipoAlojamiento;
    private int capacidad;
    private BigDecimal precioPorNoche;
    private String estado;
    private OffsetDateTime fechaCreacion;
    private String imagenPortada;
    private String pais;
    private String codigoPais;

    public PublicacionResumenResponse(
            UUID idPublicacion,
            UUID idAlojamiento,
            String titulo,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            BigDecimal precioPorNoche,
            String estado,
            OffsetDateTime fechaCreacion) {
        this(idPublicacion, idAlojamiento, titulo, ciudad, tipoAlojamiento,
                capacidad, precioPorNoche, estado, fechaCreacion, null);
    }

    @JsonCreator
    public PublicacionResumenResponse(
            @JsonProperty("idPublicacion") UUID idPublicacion,
            @JsonProperty("idAlojamiento") UUID idAlojamiento,
            @JsonProperty("titulo") String titulo,
            @JsonProperty("ciudad") String ciudad,
            @JsonProperty("tipoAlojamiento") String tipoAlojamiento,
            @JsonProperty("capacidad") int capacidad,
            @JsonProperty("precioPorNoche") BigDecimal precioPorNoche,
            @JsonProperty("estado") String estado,
            @JsonProperty("fechaCreacion") OffsetDateTime fechaCreacion,
            @JsonProperty("imagenPortada") String imagenPortada) {
        this.idPublicacion = idPublicacion;
        this.idAlojamiento = idAlojamiento;
        this.titulo = titulo;
        this.ciudad = ciudad;
        this.tipoAlojamiento = tipoAlojamiento;
        this.capacidad = capacidad;
        this.precioPorNoche = precioPorNoche;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.imagenPortada = imagenPortada;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public UUID getIdAlojamiento() {
        return idAlojamiento;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getCiudad() {
        return ciudad;
    }

    public String getTipoAlojamiento() {
        return tipoAlojamiento;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public BigDecimal getPrecioPorNoche() {
        return precioPorNoche;
    }

    public String getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public String getImagenPortada() {
        return imagenPortada;
    }

    public PublicacionResumenResponse conPais(String pais, String codigoPais) {
        this.pais = pais;
        this.codigoPais = codigoPais;
        return this;
    }

    public String getPais() {
        return pais;
    }

    public String getCodigoPais() {
        return codigoPais;
    }
}
