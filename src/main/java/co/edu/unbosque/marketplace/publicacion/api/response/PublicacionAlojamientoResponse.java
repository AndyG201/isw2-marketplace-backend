
package co.edu.unbosque.marketplace.publicacion.api.response;

import java.util.List;
import java.util.UUID;

public class PublicacionAlojamientoResponse {

    private UUID idAlojamiento;
    private UUID idPublicacion;
    private String titulo;
    private String estado;
    private List<String> imagenes;

    public PublicacionAlojamientoResponse(
            UUID idAlojamiento,
            UUID idPublicacion,
            String titulo,
            String estado,
            List<String> imagenes) {

        this.idAlojamiento = idAlojamiento;
        this.idPublicacion = idPublicacion;
        this.titulo = titulo;
        this.estado = estado;
        this.imagenes = imagenes;
    }

    public UUID getIdAlojamiento() {
        return idAlojamiento;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getEstado() {
        return estado;
    }

    public List<String> getImagenes() {
        return imagenes;
    }
}
