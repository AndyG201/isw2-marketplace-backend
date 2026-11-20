package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class AlmacenamientoImagenServiceTest {

    @TempDir
    Path directorioTemporal;

    private AlmacenamientoImagenService service;

    @BeforeEach
    void setUp() {
        service = new AlmacenamientoImagenService(
            directorioTemporal.toString()
        );
    }

    @Test
    void debeGuardarImagenJpgCorrectamente() throws IOException {
        verificarImagenGuardada(
            new byte[] {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF
            },
            ".jpg"
        );
    }

    @Test
    void debeGuardarImagenPngCorrectamente() throws IOException {
        verificarImagenGuardada(
            new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
            },
            ".png"
        );
    }

    @Test
    void debeGuardarImagenWebpCorrectamente() throws IOException {
        verificarImagenGuardada(
            new byte[] {
                'R', 'I', 'F', 'F',
                0, 0, 0, 0,
                'W', 'E', 'B', 'P'
            },
            ".webp"
        );
    }

    private void verificarImagenGuardada(
            byte[] contenido,
            String extension) throws IOException {

        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "imagen",
            "application/octet-stream",
            contenido
        );

        String ruta = service.guardar(archivo);

        assertNotNull(ruta);
        assertTrue(ruta.startsWith("/imagenes/publicaciones/"));
        assertTrue(ruta.endsWith(extension));

        String nombreArchivo =
            ruta.substring(ruta.lastIndexOf('/') + 1);

        Path archivoGuardado = directorioTemporal
            .resolve("publicaciones")
            .resolve(nombreArchivo);

        assertTrue(Files.exists(archivoGuardado));
        assertArrayEquals(contenido, Files.readAllBytes(archivoGuardado));
    }

    @Test
    void debeLanzarExcepcionCuandoElArchivoEsNulo() {
        IllegalArgumentException excepcion = assertThrows(
            IllegalArgumentException.class,
            () -> service.guardar(null)
        );

        assertEquals(
            "El archivo de imagen está vacío.",
            excepcion.getMessage()
        );
    }

    @Test
    void debeLanzarExcepcionCuandoElArchivoEstaVacio() {
        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "imagen.png",
            "image/png",
            new byte[0]
        );

        IllegalArgumentException excepcion = assertThrows(
            IllegalArgumentException.class,
            () -> service.guardar(archivo)
        );

        assertEquals(
            "El archivo de imagen está vacío.",
            excepcion.getMessage()
        );
    }

    @Test
    void debeRechazarImagenesQueSuperenLosCincoMegabytes() {
        byte[] contenido = new byte[5 * 1024 * 1024 + 1];

        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "imagen.png",
            "image/png",
            contenido
        );

        IllegalArgumentException excepcion = assertThrows(
            IllegalArgumentException.class,
            () -> service.guardar(archivo)
        );

        assertEquals(
            "Cada imagen debe pesar máximo 5 MB.",
            excepcion.getMessage()
        );
    }

    @Test
    void debeLanzarExcepcionCuandoElFormatoNoEsPermitido() {
        byte[] contenido = {
            0x01, 0x02, 0x03, 0x04
        };

        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "imagen.gif",
            "image/gif",
            contenido
        );

        IllegalArgumentException excepcion = assertThrows(
            IllegalArgumentException.class,
            () -> service.guardar(archivo)
        );

        assertEquals(
            "Formato de imagen no permitido. Utiliza JPG, PNG o WEBP.",
            excepcion.getMessage()
        );
    }

    @Test
    void debeConvertirIOExceptionEnIllegalStateException() throws IOException {
        MultipartFile archivo = mock(MultipartFile.class);

        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getSize()).thenReturn(100L);
        when(archivo.getBytes()).thenThrow(
            new IOException("Error de lectura")
        );

        IllegalStateException excepcion = assertThrows(
            IllegalStateException.class,
            () -> service.guardar(archivo)
        );

        assertEquals(
            "No fue posible almacenar la imagen.",
            excepcion.getMessage()
        );

        assertInstanceOf(IOException.class, excepcion.getCause());
    }

    @Test
    void debeEliminarImagenGuardada() throws IOException {
        byte[] contenido = {
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF
        };

        MockMultipartFile archivo = new MockMultipartFile(
            "archivo",
            "imagen.jpg",
            "image/jpeg",
            contenido
        );

        String ruta = service.guardar(archivo);

        String nombreArchivo =
            ruta.substring(ruta.lastIndexOf('/') + 1);

        Path archivoGuardado = directorioTemporal
            .resolve("publicaciones")
            .resolve(nombreArchivo);

        assertTrue(Files.exists(archivoGuardado));

        service.eliminar(ruta);

        assertFalse(Files.exists(archivoGuardado));
    }

    @Test
    void debePermitirEliminarUnArchivoInexistente() {
        assertDoesNotThrow(
            () -> service.eliminar("/imagenes/publicaciones/inexistente.jpg")
        );
    }

    @Test
    void noDebeHacerNadaCuandoLaRutaEsNula() {
        assertDoesNotThrow(() -> service.eliminar(null));
    }

    @Test
    void noDebeHacerNadaCuandoLaRutaEstaVacia() {
        assertDoesNotThrow(() -> service.eliminar(""));
    }

    @Test
    void noDebeHacerNadaCuandoLaRutaContieneEspacios() {
        assertDoesNotThrow(() -> service.eliminar("   "));
    }
}
