package co.edu.unbosque.marketplace.publicacion.infrastructure.cache;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class PublicacionBusquedaCacheWebConfig
        implements WebMvcConfigurer {

    private final PublicacionBusquedaCacheInvalidationInterceptor interceptor;

    public PublicacionBusquedaCacheWebConfig(
            PublicacionBusquedaCacheInvalidationInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}