package co.edu.unbosque.marketplace.usuario.infrastructure.redis;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;

@Component
public class AlmacenTemporalRedis implements AlmacenTemporal {

    private final StringRedisTemplate redis;

    public AlmacenTemporalRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void guardar(String clave, String valor, Duration vigencia) {
        redis.opsForValue().set(clave, valor, vigencia);
    }

    @Override
    public String obtener(String clave) {
        return redis.opsForValue().get(clave);
    }

    @Override
    public boolean guardarSiAusente(String clave, String valor, Duration vigencia) {
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(clave, valor, vigencia));
    }

    @Override
    public void eliminar(String clave) {
        redis.delete(clave);
    }

    @Override
    public Long incrementar(String clave) {
        return redis.opsForValue().increment(clave);
    }

    @Override
    public Long decrementar(String clave) {
        return redis.opsForValue().decrement(clave);
    }

    @Override
    public void asignarVigencia(String clave, Duration vigencia) {
        redis.expire(clave, vigencia);
    }

    @Override
    public Long segundosRestantes(String clave) {
        Long segundos = redis.getExpire(clave, TimeUnit.SECONDS);
        // Redis responde -2 si la clave no existe y -1 si no vence; el puerto promete nulo en ambos casos.
        return segundos == null || segundos < 0 ? null : segundos;
    }
}
