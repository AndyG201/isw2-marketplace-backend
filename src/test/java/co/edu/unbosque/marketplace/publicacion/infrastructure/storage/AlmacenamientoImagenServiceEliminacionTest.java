package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;

class AlmacenamientoImagenServiceEliminacionTest {

    @TempDir
    Path directorioTemporal;

    @Test
    void eliminarFallaConConflictoSiElSistemaDeArchivosNoPermiteBorrar() throws IOException {
        AlmacenamientoImagenService service = new AlmacenamientoImagenService(
                directorioTemporal.toString(), mock(CloudinaryCliente.class));
        // Una carpeta con contenido no se puede borrar con deleteIfExists.
        Path carpeta = directorioTemporal.resolve("publicaciones").resolve("carpeta");
        Files.createDirectories(carpeta);
        Files.writeString(carpeta.resolve("dentro.txt"), "contenido");

        assertThrows(ConflictoNegocioException.class, () -> service.eliminar("/imagenes/publicaciones/carpeta"));
    }
}
