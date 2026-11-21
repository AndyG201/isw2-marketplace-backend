package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;

class AlmacenamientoImagenServiceTest {

    private static final byte[] JPG = {
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00
    };

    @TempDir
    Path directorioTemporal;

    private AlmacenamientoImagenService service;
    private CloudinaryCliente cloudinary;

    @BeforeEach
    void setUp() {
        cloudinary = mock(CloudinaryCliente.class);

        service = new AlmacenamientoImagenService(
            directorioTemporal.toString(),
            cloudinary
        );
    }

    private MockMultipartFile archivo(byte[] contenido) {
        return new MockMultipartFile(
            "imagenes",
            "foto.jpg",
            "image/jpeg",
            contenido
        );
    }

    // ==========================================
    // PRUEBAS DE ALMACENAMIENTO LOCAL
    // ==========================================

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
        assertArrayEquals(
            contenido,
            Files.readAllBytes(archivoGuardado)
        );

        verify(cloudinary, never())
            .subir(any(byte[].class), anyString(), anyString());
    }

    @Test
    void sinCloudinaryGuardaLaImagenEnElDisco() throws IOException {
        String ruta = service.guardar(archivo(JPG));

        assertTrue(ruta.startsWith("/imagenes/publicaciones/"));
        assertTrue(ruta.endsWith(".jpg"));

        Path guardada = directorioTemporal
            .resolve("publicaciones")
            .resolve(ruta.substring(ruta.lastIndexOf('/') + 1));

        assertTrue(Files.exists(guardada));

        verify(cloudinary, never())
            .subir(any(byte[].class), anyString(), anyString());
    }

    // ==========================================
    // PRUEBAS DE CLOUDINARY
    // ==========================================

    @Test
    void conCloudinarySubeLaImagenYNoTocaElDisco() {
        when(cloudinary.configurado()).thenReturn(true);

        String urlEsperada =
            "https://res.cloudinary.com/demo/image/upload/v1/a.jpg";

        when(cloudinary.subir(
            any(byte[].class),
            anyString(),
            eq(".jpg")
        )).thenReturn(urlEsperada);

        String ruta = service.guardar(archivo(JPG));

        assertEquals(urlEsperada, ruta);

        assertFalse(
            Files.exists(
                directorioTemporal.resolve("publicaciones")
            )
        );

        verify(cloudinary).subir(
            any(byte[].class),
            anyString(),
            eq(".jpg")
        );
    }

    @Test
    void eliminaEnCloudinaryLasImagenesDeLaCuenta() {
        String url =
            "https://res.cloudinary.com/demo/image/upload/v1/a.jpg";

        when(cloudinary.esUrlPropia(url)).thenReturn(true);

        service.eliminar(url);

        verify(cloudinary).eliminar(url);
    }

    @Test
    void ignoraDireccionesExternasQueNoSonDeLaCuenta() {
        String url =
            "https://res.cloudinary.com/otra/image/upload/v1/a.jpg";

        service.eliminar(url);

        verify(cloudinary, never()).eliminar(anyString());
    }

    // ==========================================
    // PRUEBAS DE VALIDACIONES
    // ==========================================

    @Test
    void debeLanzarExcepcionCuandoElArchivoEsNulo() {
        DatosInvalidosException excepcion = assertThrows(
            DatosInvalidosException.class,
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

        DatosInvalidosException excepcion = assertThrows(
            DatosInvalidosException.class,
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

        DatosInvalidosException excepcion = assertThrows(
            DatosInvalidosException.class,
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

        DatosInvalidosException excepcion = assertThrows(
            DatosInvalidosException.class,
            () -> service.guardar(archivo)
        );

        assertEquals(
            "Formato de imagen no permitido. Utiliza JPG, PNG o WEBP.",
            excepcion.getMessage()
        );

        verify(cloudinary, never())
            .subir(any(byte[].class), anyString(), anyString());
    }

    @Test
    void debeConvertirIOExceptionEnIllegalStateException()
            throws IOException {

        MultipartFile archivo = mock(MultipartFile.class);

        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getSize()).thenReturn(100L);
        when(archivo.getBytes()).thenThrow(
            new IOException("Error de lectura")
        );

        ConflictoNegocioException excepcion = assertThrows(
            ConflictoNegocioException.class,
            () -> service.guardar(archivo)
        );

        assertEquals(
            "No fue posible almacenar la imagen.",
            excepcion.getMessage()
        );

        assertInstanceOf(IOException.class, excepcion.getCause());
    }

    @Test
    void rechazaArchivosQueNoSonImagenes() {
        MockMultipartFile texto = archivo(
            "hola".getBytes(StandardCharsets.UTF_8)
        );

        assertThrows(
            DatosInvalidosException.class,
            () -> service.guardar(texto)
        );

        verify(cloudinary, never())
            .subir(any(byte[].class), anyString(), anyString());
    }

    // ==========================================
    // PRUEBAS DE ELIMINACION LOCAL
    // ==========================================

    @Test
    void debeEliminarImagenGuardada() throws IOException {
        String ruta = service.guardar(archivo(JPG));

        String nombreArchivo =
            ruta.substring(ruta.lastIndexOf('/') + 1);

        Path archivoGuardado = directorioTemporal
            .resolve("publicaciones")
            .resolve(nombreArchivo);

        assertTrue(Files.exists(archivoGuardado));

        service.eliminar(ruta);

        assertFalse(Files.exists(archivoGuardado));

        verify(cloudinary, never()).eliminar(anyString());
    }

    @Test
    void debePermitirEliminarUnArchivoInexistente() {
        assertDoesNotThrow(
            () -> service.eliminar(
                "/imagenes/publicaciones/inexistente.jpg"
            )
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
