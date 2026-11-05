
package co.edu.unbosque.marketplace.publicacion.api.response;

import java.util.UUID;

public class ImagenPublicacionResponse {

    private UUID idImagen;
    private String rutaImagen;
    private int orden;
    private boolean esPortada;

    public ImagenPublicacionResponse(
            UUID idImagen,
            String rutaImagen,
            int orden,
            boolean esPortada) {

        this.idImagen = idImagen;
        this.rutaImagen = rutaImagen;
        this.orden = orden;
        this.esPortada = esPortada;
    }

    public UUID getIdImagen() { return idImagen; }
    public String getRutaImagen() { return rutaImagen; }
    public int getOrden() { return orden; }
    public boolean isEsPortada() { return esPortada; }
}
