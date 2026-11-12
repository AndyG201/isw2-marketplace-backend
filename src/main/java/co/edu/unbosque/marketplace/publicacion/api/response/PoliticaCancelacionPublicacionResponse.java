package co.edu.unbosque.marketplace.publicacion.api.response;

import java.math.BigDecimal;
import java.util.UUID;

public class PoliticaCancelacionPublicacionResponse {

    private UUID idPublicacion;
    private UUID idPolitica;
    private String nombre;
    private int version;
    private String descripcion;
    private Integer anticipacionHoras;
    private BigDecimal porcentajeDevolucion;
    private boolean activa;

    public PoliticaCancelacionPublicacionResponse(
            UUID idPublicacion,
            UUID idPolitica,
            String nombre,
            int version,
            String descripcion,
            Integer anticipacionHoras,
            BigDecimal porcentajeDevolucion,
            boolean activa) {

        this.idPublicacion = idPublicacion;
        this.idPolitica = idPolitica;
        this.nombre = nombre;
        this.version = version;
        this.descripcion = descripcion;
        this.anticipacionHoras = anticipacionHoras;
        this.porcentajeDevolucion = porcentajeDevolucion;
        this.activa = activa;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public UUID getIdPolitica() {
        return idPolitica;
    }

    public String getNombre() {
        return nombre;
    }

    public int getVersion() {
        return version;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getAnticipacionHoras() {
        return anticipacionHoras;
    }

    public BigDecimal getPorcentajeDevolucion() {
        return porcentajeDevolucion;
    }

    public boolean isActiva() {
        return activa;
    }
}
