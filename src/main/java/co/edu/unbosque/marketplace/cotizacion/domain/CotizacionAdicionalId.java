package co.edu.unbosque.marketplace.cotizacion.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CotizacionAdicionalId implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID idCotizacion;
    private UUID idAdicional;

    public CotizacionAdicionalId() {
    }

    public CotizacionAdicionalId(UUID idCotizacion, UUID idAdicional) {
        this.idCotizacion = idCotizacion;
        this.idAdicional = idAdicional;
    }

    public UUID getIdCotizacion() {
        return idCotizacion;
    }

    public void setIdCotizacion(UUID idCotizacion) {
        this.idCotizacion = idCotizacion;
    }

    public UUID getIdAdicional() {
        return idAdicional;
    }

    public void setIdAdicional(UUID idAdicional) {
        this.idAdicional = idAdicional;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof CotizacionAdicionalId otro)) {
            return false;
        }
        return Objects.equals(idCotizacion, otro.idCotizacion)
                && Objects.equals(idAdicional, otro.idAdicional);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCotizacion, idAdicional);
    }
}