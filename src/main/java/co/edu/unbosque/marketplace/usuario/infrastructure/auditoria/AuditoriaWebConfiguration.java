package co.edu.unbosque.marketplace.usuario.infrastructure.auditoria;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuditoriaWebConfiguration implements WebMvcConfigurer {

    private final AuditoriaErroHttpInterceptor interceptor;

    public AuditoriaWebConfiguration(AuditoriaErroHttpInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}