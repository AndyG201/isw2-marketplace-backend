package co.edu.unbosque.marketplace.publicacion.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PublicacionDetalleResponse {

    private UUID idPublicacion;
    private UUID idAlojamiento;
    private UUID idPolitica;
    private String titulo;
    private String descripcion;
    private String reglas;
    private BigDecimal precioPorNoche;
    private BigDecimal tarifaLimpieza;
    private String modoAceptacion;
    private String estado;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaActualizacion;

    private String ubicacion;
    private String ciudad;
    private String tipoAlojamiento;
    private int capacidad;

    private List<ServicioPublicacionResponse> servicios;
    private List<ImagenPublicacionResponse> imagenes;

    public PublicacionDetalleResponse(
            UUID idPublicacion,
            UUID idAlojamiento,
            UUID idPolitica,
            String titulo,
            String descripcion,
            String reglas,
            BigDecimal precioPorNoche,
            BigDecimal tarifaLimpieza,
            String modoAceptacion,
            String estado,
            OffsetDateTime fechaCreacion,
            OffsetDateTime fechaActualizacion,
            String ubicacion,
            String ciudad,
            String tipoAlojamiento,
            int capacidad,
            List<ServicioPublicacionResponse> servicios,
            List<ImagenPublicacionResponse> imagenes) {

        this.idPublicacion = idPublicacion;
        this.idAlojamiento = idAlojamiento;
        this.idPolitica = idPolitica;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.reglas = reglas;
        this.precioPorNoche = precioPorNoche;
        this.tarifaLimpieza = tarifaLimpieza;
        this.modoAceptacion = modoAceptacion;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
        this.ubicacion = ubicacion;
        this.ciudad = ciudad;
        this.tipoAlojamiento = tipoAlojamiento;
        this.capacidad = capacidad;
		this.servicios = servicios;
        this.imagenes = imagenes;
    }

    public UUID getIdPublicacion() { return idPublicacion; }
    public UUID getIdAlojamiento() { return idAlojamiento; }
    public UUID getIdPolitica() { return idPolitica; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getReglas() { return reglas; }
    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public BigDecimal getTarifaLimpieza() { return tarifaLimpieza; }
    public String getModoAceptacion() { return modoAceptacion; }
    public String getEstado() { return estado; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public OffsetDateTime getFechaActualizacion() { return fechaActualizacion; }
    public String getUbicacion() { return ubicacion; }
    public String getCiudad() { return ciudad; }
    public String getTipoAlojamiento() { return tipoAlojamiento; }
    public int getCapacidad() { return capacidad; }
    public List<ServicioPublicacionResponse> getServicios() {
        return servicios;
    }
    public List<ImagenPublicacionResponse> getImagenes() { return imagenes; }
}
