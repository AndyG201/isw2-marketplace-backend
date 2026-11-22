package co.edu.unbosque.marketplace.auditoria.infrastructure.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.auditoria.application.port.CacheEventosAuditoria;
import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;

/** Con un Redis real: lo que se guarda se lee idéntico, expira y una huella distinta no encuentra la entrada. */
@SpringBootTest
@ActiveProfiles("test")
class CacheEventosAuditoriaRedisIntegracionTest {

    private static final String SHA = "b".repeat(64);

    @Autowired private CacheEventosAuditoria cache;
    @Autowired private StringRedisTemplate redis;

    private final EventoAuditoria evento = new EventoAuditoria(UUID.randomUUID(), UUID.randomUUID(),
            OffsetDateTime.parse("2026-11-20T10:15:30-05:00"), "PAGO_APROBADO", "EXITOSO", "PAGO", UUID.randomUUID(),
            "{\"monto\":220000}", "correlacion-xyz");

    @AfterEach
    void limpiar() {
        redis.delete(CacheEventosAuditoriaRedis.clave(evento.idEvento(), SHA));
    }

    @Test
    void guardaYLeeElMismoEventoConVigencia() {
        assertTrue(cache.leer(evento.idEvento(), SHA).isEmpty());

        cache.guardar(SHA, evento);

        assertEquals(evento, cache.leer(evento.idEvento(), SHA).orElseThrow());
        Long segundos = redis.getExpire(CacheEventosAuditoriaRedis.clave(evento.idEvento(), SHA));
        assertTrue(segundos != null && segundos > 11 * 3600 && segundos <= 12 * 3600, "vigencia: " + segundos);
    }

    @Test
    void conOtraHuellaNoSeEncuentraLaEntrada() {
        cache.guardar(SHA, evento);

        assertTrue(cache.leer(evento.idEvento(), "c".repeat(64)).isEmpty());
    }
}
