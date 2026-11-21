package co.edu.unbosque.marketplace.usuario.infrastructure.redis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class AlmacenTemporalRedisTest {

    @Test
    @SuppressWarnings("unchecked")
    void guardarSiAusenteEsFalsoSiRedisNoResponde() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> valores = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valores);
        when(valores.setIfAbsent("clave", "valor", Duration.ofSeconds(60))).thenReturn(null);

        assertFalse(new AlmacenTemporalRedis(redis).guardarSiAusente("clave", "valor", Duration.ofSeconds(60)));
    }
}
