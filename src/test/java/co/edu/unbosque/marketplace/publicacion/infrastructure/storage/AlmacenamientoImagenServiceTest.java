package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class AlmacenamientoImagenServiceTest {

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};

    @TempDir
    Path directorio;

    private final CloudinaryCliente cloudinary = mock(CloudinaryCliente.class);

    private AlmacenamientoImagenService servicio() {
        return new AlmacenamientoImagenService(directorio.toString(), cloudinary);
    }

    private MockMultipartFile archivo(byte[] contenido) {
        return new MockMultipartFile("imagenes", "foto.jpg", "image/jpeg", contenido);
    }

    @Test
    void sinCloudinaryGuardaLaImagenEnElDisco() throws Exception {
        String ruta = servicio().guardar(archivo(JPG));

        assertTrue(ruta.startsWith("/imagenes/publicaciones/"));
        assertTrue(ruta.endsWith(".jpg"));
        Path guardada = directorio.resolve("publicaciones").resolve(ruta.substring(ruta.lastIndexOf('/') + 1));
        assertTrue(Files.exists(guardada));
        verify(cloudinary, never()).subir(any(), anyString(), anyString());
    }

    @Test
    void conCloudinarySubeLaImagenYNoTocaElDisco() {
        when(cloudinary.configurado()).thenReturn(true);
        when(cloudinary.subir(eq(JPG), anyString(), eq(".jpg")))
                .thenReturn("https://res.cloudinary.com/demo/image/upload/v1/a.jpg");

        String ruta = servicio().guardar(archivo(JPG));

        assertEquals("https://res.cloudinary.com/demo/image/upload/v1/a.jpg", ruta);
        assertFalse(Files.exists(directorio.resolve("publicaciones")));
    }

    @Test
    void rechazaArchivosQueNoSonImagenes() {
        AlmacenamientoImagenService servicio = servicio();
        MockMultipartFile texto = archivo("hola".getBytes());

        assertThrows(IllegalArgumentException.class, () -> servicio.guardar(texto));
        verify(cloudinary, never()).subir(any(), anyString(), anyString());
    }

    @Test
    void eliminaEnCloudinaryLasImagenesDeLaCuenta() {
        String url = "https://res.cloudinary.com/demo/image/upload/v1/a.jpg";
        when(cloudinary.esUrlPropia(url)).thenReturn(true);

        servicio().eliminar(url);

        verify(cloudinary).eliminar(url);
    }

    @Test
    void ignoraDireccionesExternasQueNoSonDeLaCuenta() {
        servicio().eliminar("https://res.cloudinary.com/otra/image/upload/v1/a.jpg");

        verify(cloudinary, never()).eliminar(anyString());
    }

    @Test
    void eliminaDelDiscoLasImagenesLocales() throws Exception {
        AlmacenamientoImagenService servicio = servicio();
        String ruta = servicio.guardar(archivo(JPG));
        Path guardada = directorio.resolve("publicaciones").resolve(ruta.substring(ruta.lastIndexOf('/') + 1));

        servicio.eliminar(ruta);

        assertFalse(Files.exists(guardada));
    }
}
