
package co.edu.unbosque.marketplace.publicacion.infrastructure.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ImagenPublicacionWebConfig implements WebMvcConfigurer {

    private final Path directorioBase;

    public ImagenPublicacionWebConfig(
            @Value("${app.storage.upload-dir:uploads}")
            String directorio) {

        this.directorioBase = Paths.get(directorio)
                .toAbsolutePath()
                .normalize()
                .resolve("publicaciones")
                .normalize();
    }

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry) {

        String ubicacion = directorioBase.toUri().toString();

        if (!ubicacion.endsWith("/")) {
            ubicacion += "/";
        }

        registry.addResourceHandler(
                "/imagenes/publicaciones/**"
        ).addResourceLocations(ubicacion);
    }
}
