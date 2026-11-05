package co.edu.unbosque.marketplace.publicacion.api.response;
import java.util.List;

public class MisPublicacionesResponse {

    private String mensaje;
    private List<PublicacionResumenResponse> publicaciones;

    public MisPublicacionesResponse(
            String mensaje,
            List<PublicacionResumenResponse> publicaciones) {

        this.mensaje = mensaje;
        this.publicaciones = publicaciones;
    }

    public String getMensaje() {
        return mensaje;
    }

    public List<PublicacionResumenResponse> getPublicaciones() {
        return publicaciones;
    }
}
