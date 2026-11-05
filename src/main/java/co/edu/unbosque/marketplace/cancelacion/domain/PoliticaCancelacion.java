package co.edu.unbosque.marketplace.cancelacion.domain;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "politica_cancelacion")
public class PoliticaCancelacion {

    @Id
    @Column(name = "id_politica", nullable = false)
    private UUID idPolitica;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "descripcion", columnDefinition = "text")
    private String descripcion;

    @Column(name = "anticipacion_horas")
    private Integer anticipacionHoras;

    @Column(name = "porcentaje_devolucion", precision = 5, scale = 2, nullable = false)
    private BigDecimal porcentajeDevolucion;

    @Column(name = "activa", nullable = false)
    private boolean activa = true;

    public PoliticaCancelacion() {
    }

    public UUID getIdPolitica() {
        return idPolitica;
    }

    public void setIdPolitica(UUID idPolitica) {
        this.idPolitica = idPolitica;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getAnticipacionHoras() {
        return anticipacionHoras;
    }

    public void setAnticipacionHoras(Integer anticipacionHoras) {
        this.anticipacionHoras = anticipacionHoras;
    }

    public BigDecimal getPorcentajeDevolucion() {
        return porcentajeDevolucion;
    }

    public void setPorcentajeDevolucion(BigDecimal porcentajeDevolucion) {
        this.porcentajeDevolucion = porcentajeDevolucion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
