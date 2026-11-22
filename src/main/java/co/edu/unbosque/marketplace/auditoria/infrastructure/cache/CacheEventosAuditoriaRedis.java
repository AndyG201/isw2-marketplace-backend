package co.edu.unbosque.marketplace.auditoria.infrastructure.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import co.edu.unbosque.marketplace.auditoria.application.port.CacheEventosAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;

@Component
public class CacheEventosAuditoriaRedis implements CacheEventosAuditoria {

    private static final Logger LOGGER = Logger.getLogger(CacheEventosAuditoriaRedis.class.getName());

    static final String PREFIJO = "marketplace:auditoria:evento:";

    /** Los eventos son inmutables; la vigencia solo limita cuánta memoria ocupan los que ya nadie consulta. */
    static final Duration VIGENCIA = Duration.ofHours(12);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public CacheEventosAuditoriaRedis(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        // La fecha se guarda como texto ISO con su desfase y se lee igual, sin convertirla a UTC.
        this.objectMapper = objectMapper.copy()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);
    }

    @Override
    public Optional<EventoAuditoria> leer(UUID idEvento, String sha256) {
        String clave = clave(idEvento, sha256);
        try {
            String json = redis.opsForValue().get(clave);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, EventoAuditoria.class));
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.WARNING, "Entrada ilegible en la caché de auditoría; se descarta.", e);
            descartar(clave);
            return Optional.empty();
        } catch (RuntimeException e) {
            LOGGER.log(Level.FINE, "Redis no disponible; el evento se leerá del almacén.", e);
            return Optional.empty();
        }
    }

    @Override
    public void guardar(String sha256, EventoAuditoria evento) {
        try {
            redis.opsForValue().set(clave(evento.idEvento(), sha256), objectMapper.writeValueAsString(evento), VIGENCIA);
        } catch (JsonProcessingException | RuntimeException e) {
            LOGGER.log(Level.FINE, "No se pudo guardar el evento de auditoría en la caché.", e);
        }
    }

    static String clave(UUID idEvento, String sha256) {
        return PREFIJO + idEvento + ":" + sha256;
    }

    private void descartar(String clave) {
        try {
            redis.delete(clave);
        } catch (RuntimeException ignorada) {
            // Con Redis caído no hay nada que limpiar.
        }
    }
}
