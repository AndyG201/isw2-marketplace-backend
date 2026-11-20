package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;

class ImagenPublicacionWebConfigTest {

    @TempDir
    Path directorioTemporal;

    private ImagenPublicacionWebConfig configuracion;

    @BeforeEach
    void setUp() {
        configuracion = new ImagenPublicacionWebConfig(
                directorioTemporal.toString()
        );
    }

    @Test
    void debeRegistrarLaRutaDeImagenesPublicas() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/imagenes/publicaciones/**"))
                .thenReturn(registration);

        configuracion.addResourceHandlers(registry);

        verify(registry).addResourceHandler("/imagenes/publicaciones/**");
    }

    @Test
    void debeConfigurarElDirectorioDePublicaciones() {
        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/imagenes/publicaciones/**"))
                .thenReturn(registration);

        configuracion.addResourceHandlers(registry);

        Path directorioEsperado = directorioTemporal
                .toAbsolutePath()
                .normalize()
                .resolve("publicaciones")
                .normalize();

        String ubicacionEsperada = directorioEsperado.toUri().toString();

        if (!ubicacionEsperada.endsWith("/")) {
            ubicacionEsperada += "/";
        }

        verify(registration).addResourceLocations(ubicacionEsperada);
    }

    @Test
    void debeAceptarUnDirectorioConSeparadorFinal() {
        String directorio = directorioTemporal.toString()
                + java.io.File.separator;

        ImagenPublicacionWebConfig otraConfiguracion =
                new ImagenPublicacionWebConfig(directorio);

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);

        when(registry.addResourceHandler("/imagenes/publicaciones/**"))
                .thenReturn(registration);

        otraConfiguracion.addResourceHandlers(registry);

        Path directorioEsperado = Paths.get(directorio)
                .toAbsolutePath()
                .normalize()
                .resolve("publicaciones")
                .normalize();

        String ubicacionEsperada = directorioEsperado.toUri().toString();

        if (!ubicacionEsperada.endsWith("/")) {
            ubicacionEsperada += "/";
        }

        verify(registration).addResourceLocations(ubicacionEsperada);
    }
}