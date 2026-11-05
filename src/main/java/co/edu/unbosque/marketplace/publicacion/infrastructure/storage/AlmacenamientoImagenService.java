
package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlmacenamientoImagenService {

    private static final long TAMANO_MAXIMO = 5 * 1024 * 1024;

    private final Path directorioBase;

    public AlmacenamientoImagenService(
            @Value("${app.storage.upload-dir:uploads}")
            String directorio) {

        this.directorioBase = Paths.get(directorio)
                .toAbsolutePath()
                .normalize()
                .resolve("publicaciones")
                .normalize();
    }

    public String guardar(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El archivo de imagen está vacío.");
        }

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "Cada imagen debe pesar máximo 5 MB.");
        }

        try {

            byte[] contenido = archivo.getBytes();

            String extension = detectarExtension(contenido);

            String nombreArchivo =
                    UUID.randomUUID() + extension;

            Files.createDirectories(directorioBase);

            Path destino = directorioBase
                    .resolve(nombreArchivo)
                    .normalize();

            if (!destino.startsWith(directorioBase)) {
                throw new IllegalArgumentException(
                        "La ruta del archivo no es válida.");
            }

            Files.write(
                    destino,
                    contenido,
                    StandardOpenOption.CREATE_NEW
            );

            return "/imagenes/publicaciones/" + nombreArchivo;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No fue posible almacenar la imagen.", e);
        }
    }

    private String detectarExtension(byte[] contenido) {

        if (contenido.length >= 3
                && (contenido[0] & 0xFF) == 0xFF
                && (contenido[1] & 0xFF) == 0xD8
                && (contenido[2] & 0xFF) == 0xFF) {

            return ".jpg";
        }

        if (contenido.length >= 8
                && (contenido[0] & 0xFF) == 0x89
                && contenido[1] == 0x50
                && contenido[2] == 0x4E
                && contenido[3] == 0x47
                && contenido[4] == 0x0D
                && contenido[5] == 0x0A
                && contenido[6] == 0x1A
                && contenido[7] == 0x0A) {

            return ".png";
        }

        if (contenido.length >= 12
                && contenido[0] == 'R'
                && contenido[1] == 'I'
                && contenido[2] == 'F'
                && contenido[3] == 'F'
                && contenido[8] == 'W'
                && contenido[9] == 'E'
                && contenido[10] == 'B'
                && contenido[11] == 'P') {

            return ".webp";
        }

        throw new IllegalArgumentException(
                "Formato de imagen no permitido. Utiliza JPG, PNG o WEBP.");
    }

    public void eliminar(String rutaImagen) {

        if (rutaImagen == null || rutaImagen.isBlank()) {
            return;
        }

        try {

            String nombreArchivo =
                    Paths.get(rutaImagen).getFileName().toString();

            Path archivo = directorioBase
                    .resolve(nombreArchivo)
                    .normalize();

            if (archivo.startsWith(directorioBase)) {
                Files.deleteIfExists(archivo);
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "No fue posible eliminar la imagen.", e);
        }
    }
}
