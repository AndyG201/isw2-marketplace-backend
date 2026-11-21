package co.edu.unbosque.marketplace.publicacion.application.port;

import org.springframework.web.multipart.MultipartFile;

public interface AlmacenamientoImagenes {

    String guardar(MultipartFile archivo);

    void eliminar(String rutaImagen);
}
