package co.edu.unbosque.marketplace.publicacion.api.response;

import java.math.BigDecimal;
import java.util.UUID;

public class ServicioAdicionalPublicacionResponse {

    private UUID idAdicional;
    private String nombre;
    private String descripcion;
    private BigDecimal costo;
    private boolean activo;

    public ServicioAdicionalPublicacionResponse(
            UUID idAdicional,
            String nombre,
            String descripcion,
            BigDecimal costo,
            boolean activo) {

        this.idAdicional = idAdicional;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costo = costo;
        this.activo = activo;
    }

    public UUID getIdAdicional() {
        return idAdicional;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public boolean isActivo() {
        return activo;
    }
}
