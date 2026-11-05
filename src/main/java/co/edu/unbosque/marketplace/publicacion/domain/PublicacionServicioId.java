
package co.edu.unbosque.marketplace.publicacion.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class PublicacionServicioId implements Serializable {

    private UUID idPublicacion;
    private UUID idServicio;

    public PublicacionServicioId() {
    }

    public PublicacionServicioId(UUID idPublicacion, UUID idServicio) {
        this.idPublicacion = idPublicacion;
        this.idServicio = idServicio;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public void setIdPublicacion(UUID idPublicacion) {
        this.idPublicacion = idPublicacion;
    }

    public UUID getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(UUID idServicio) {
        this.idServicio = idServicio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof PublicacionServicioId)) return false;

        PublicacionServicioId that = (PublicacionServicioId) o;

        return Objects.equals(idPublicacion, that.idPublicacion)
                && Objects.equals(idServicio, that.idServicio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPublicacion, idServicio);
    }
}
