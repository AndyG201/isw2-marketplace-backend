
package co.edu.unbosque.marketplace.publicacion.api.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

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

        this.idPublicacion = idPublicacion;
        this.idAlojamiento = idAlojamiento;
        this.titulo = titulo;
        this.ciudad = ciudad;
        this.tipoAlojamiento = tipoAlojamiento;
        this.capacidad = capacidad;
        this.precioPorNoche = precioPorNoche;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
    }

    public UUID getIdPublicacion() { return idPublicacion; }
    public UUID getIdAlojamiento() { return idAlojamiento; }
    public String getTitulo() { return titulo; }
    public String getCiudad() { return ciudad; }
    public String getTipoAlojamiento() { return tipoAlojamiento; }
    public int getCapacidad() { return capacidad; }
    public BigDecimal getPrecioPorNoche() { return precioPorNoche; }
    public String getEstado() { return estado; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
}
