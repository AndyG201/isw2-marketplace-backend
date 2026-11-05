
package co.edu.unbosque.marketplace.servicio.api.response;

import java.util.UUID;

public class ServicioResponse {

    private UUID idServicio;
    private String nombre;
    private String descripcion;

    public ServicioResponse(
            UUID idServicio,
            String nombre,
            String descripcion) {

        this.idServicio = idServicio;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public UUID getIdServicio() {
        return idServicio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
