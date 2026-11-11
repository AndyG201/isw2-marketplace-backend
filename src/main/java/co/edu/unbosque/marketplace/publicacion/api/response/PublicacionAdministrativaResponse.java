package co.edu.unbosque.marketplace.publicacion.api.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PublicacionAdministrativaResponse {

    private UUID idPublicacion;
    private UUID idAlojamiento;
    private String titulo;
    private String estado;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaActualizacion;

    public PublicacionAdministrativaResponse(
            UUID idPublicacion,
            UUID idAlojamiento,
            String titulo,
            String estado,
            OffsetDateTime fechaCreacion,
            OffsetDateTime fechaActualizacion) {

        this.idPublicacion = idPublicacion;
        this.idAlojamiento = idAlojamiento;
        this.titulo = titulo;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }

    public UUID getIdPublicacion() {
        return idPublicacion;
    }

    public UUID getIdAlojamiento() {
        return idAlojamiento;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getEstado() {
        return estado;
    }

    public OffsetDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public OffsetDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}
