package co.edu.unbosque.marketplace.publicacion.infrastructure.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.publicacion.api.response.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;


@Component
public class PublicacionBusquedaCache {

    private static final Logger LOGGER =
            Logger.getLogger(PublicacionBusquedaCache.class.getName());

    private static final String VERSION_KEY =
            "marketplace:publicaciones:busqueda:version";

    private static final String CACHE_PREFIX =
            "marketplace:publicaciones:busqueda:";

    private static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public PublicacionBusquedaCache(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String versionActual() {
        try {
            String version =
                    redisTemplate.opsForValue().get(VERSION_KEY);

            if (version == null) {
                redisTemplate.opsForValue()
                        .setIfAbsent(VERSION_KEY, "0");
                version = redisTemplate.opsForValue().get(VERSION_KEY);
            }

            return version;
        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.FINE,
                    "Redis no disponible; se consultará la base de datos.",
                    e
            );
            return null;
        }
    }

    public String clave(
            String version,
            PublicacionBusquedaCriteria criteria) {
        String canonico = String.join(
                "|",
                value(criteria.ciudad()),
                value(criteria.fechaInicio()),
                value(criteria.fechaFin()),
                value(criteria.capacidad()),
                value(criteria.tipoAlojamiento()),
                value(criteria.precioMinimo()),
                value(criteria.precioMaximo()),
                criteria.servicios().stream()
                        .map(Object::toString)
                        .sorted()
                        .collect(java.util.stream.Collectors.joining(",")),
                value(criteria.pagina()),
                value(criteria.tamano())
        );

        return CACHE_PREFIX + version + ":" + sha256(canonico);
    }

    public PaginaPublicacionesResponse leer(String key) {
        try {
            String json = redisTemplate.opsForValue().get(key);

            return json == null
                    ? null
                    : objectMapper.readValue(
                            json,
                            PaginaPublicacionesResponse.class
                    );
        } catch (JsonProcessingException e) {
            try {
                redisTemplate.delete(key);
            } catch (RuntimeException ignored) {
                // La búsqueda debe poder continuar aunque Redis falle.
            }

            LOGGER.log(
                    Level.WARNING,
                    "No se pudo leer una entrada de caché de búsqueda.",
                    e
            );
            return null;
        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.FINE,
                    "Redis no disponible; se consultará la base de datos.",
                    e
            );
            return null;
        }
    }

    public void guardar(
            String key,
            PaginaPublicacionesResponse resultado) {
        try {
            redisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(resultado),
                    TTL
            );
        } catch (JsonProcessingException | RuntimeException e) {
            LOGGER.log(
                    Level.FINE,
                    "No se pudo guardar la búsqueda en caché.",
                    e
            );
        }
    }

    public void invalidar() {
        try {
            redisTemplate.opsForValue().increment(VERSION_KEY);
        } catch (RuntimeException e) {
            LOGGER.log(
                    Level.WARNING,
                    "No se pudo invalidar la caché; expirará en 60 segundos.",
                    e
            );
        }
    }

    private static String value(Object value) {
        return value == null ? "" : value.toString();
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 no está disponible.",
                    e
            );
        }
    }
}