package co.edu.unbosque.marketplace.usuario.infrastructure.auditoria;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.auditoria.contracts.RegistrarEventoAuditoria;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditoriaErroHttpInterceptor implements HandlerInterceptor {

    private final AuditoriaEventoCommand auditoria;
    private final ObjectMapper objectMapper;

    public AuditoriaErroHttpInterceptor(
            AuditoriaEventoCommand auditoria,
            ObjectMapper objectMapper) {
        this.auditoria = auditoria;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {
        int estado = response.getStatus();
        if (estado < 400 || ignorar(request.getRequestURI())) {
            return;
        }

        Map<String, Object> detalle = new LinkedHashMap<>();
        detalle.put("metodo", request.getMethod());
        detalle.put("ruta", request.getRequestURI());
        detalle.put("estadoHttp", estado);
        detalle.put("excepcion", exception == null
                ? null
                : exception.getClass().getSimpleName());

        String json;
        try {
            json = objectMapper.writeValueAsString(detalle);
        } catch (Exception error) {
            json = "{\"estadoHttp\":" + estado + "}";
        }

        String correlacion = request.getHeader("X-Correlation-Id");
        if (correlacion != null && correlacion.length() > 100) {
            correlacion = correlacion.substring(0, 100);
        }

        try {
            auditoria.registrarEventoIndependiente(new RegistrarEventoAuditoria(
                    idUsuarioAutenticado(),
                    OffsetDateTime.now(ZoneOffset.UTC),
                    "HTTP_" + request.getMethod() + "_" + estado,
                    "FALLIDO",
                    "ENDPOINT",
                    null,
                    json,
                    correlacion));
        } catch (RuntimeException ignored) {
            // Un problema de auditoría no debe cambiar la respuesta que recibió el cliente.
        }
    }

    private UUID idUsuarioAutenticado() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion != null && autenticacion.getPrincipal() instanceof Usuario usuario) {
            return usuario.getIdUsuario();
        }
        return null;
    }

    private boolean ignorar(String ruta) {
        return ruta.startsWith("/swagger-ui")
                || ruta.startsWith("/v3/api-docs")
                || ruta.startsWith("/actuator")
                || ruta.equals("/error")
                || ruta.equals("/autenticacion/login");
    }
}