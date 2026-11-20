package co.edu.unbosque.marketplace.trabajo;

import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ColaTrabajosRedis {

    public static final String CLAVE = "marketplace:trabajos:prioridad";

    private final StringRedisTemplate redis;

    public ColaTrabajosRedis(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void encolar(TipoTrabajo tipo, String identificador) {
        String miembro = tipo.name() + "|" + identificador;

        // Primero se compara la prioridad; el tiempo ordena tareas de igual prioridad.
        double puntuacion =
                tipo.getPrioridad() * 1_000_000_000_000d + System.currentTimeMillis();

        redis.opsForZSet().addIfAbsent(CLAVE, miembro, puntuacion);
    }

    public Optional<Trabajo> tomarSiguiente() {
        var minimo = redis.opsForZSet().popMin(CLAVE);
        if (minimo == null || minimo.getValue() == null) {
            return Optional.empty();
        }

        String[] partes = minimo.getValue().split("\\|", 2);
        if (partes.length != 2) {
            return Optional.empty();
        }

        try {
            return Optional.of(new Trabajo(
                    TipoTrabajo.valueOf(partes[0]),
                    partes[1]));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public record Trabajo(TipoTrabajo tipo, String identificador) {
    }
}