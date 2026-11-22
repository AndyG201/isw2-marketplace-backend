package co.edu.unbosque.marketplace.auditoria.infrastructure.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.auditoria.domain.EventoAuditoria;

class CacheEventosAuditoriaRedisTest {

    private static final String SHA = "a".repeat(64);

    private StringRedisTemplate redis;
    private ValueOperations<String, String> valores;
    private CacheEventosAuditoriaRedis cache;
    private EventoAuditoria evento;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void preparar() {
        redis = mock(StringRedisTemplate.class);
        valores = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valores);
        cache = new CacheEventosAuditoriaRedis(redis, new ObjectMapper().findAndRegisterModules());
        evento = new EventoAuditoria(UUID.randomUUID(), UUID.randomUUID(),
                OffsetDateTime.parse("2026-11-20T10:15:30-05:00"), "RESERVA_CONFIRMADA", "EXITOSO", "RESERVA",
                UUID.randomUUID(), "{\"estado\":\"CONFIRMADA\"}", "correlacion-1");
    }

    @Test
    void laClaveIncluyeElIdYLaHuellaDelEvento() {
        assertEquals("marketplace:auditoria:evento:" + evento.idEvento() + ":" + SHA,
                CacheEventosAuditoriaRedis.clave(evento.idEvento(), SHA));
    }

    @Test
    void guardaElEventoEnJsonConVigencia() {
        cache.guardar(SHA, evento);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(valores).set(eq(CacheEventosAuditoriaRedis.clave(evento.idEvento(), SHA)), json.capture(),
                eq(CacheEventosAuditoriaRedis.VIGENCIA));
        assertTrue(json.getValue().contains("RESERVA_CONFIRMADA"));
    }

    @Test
    void leeLoQueGuardoSinPerderNingunCampo() {
        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        cache.guardar(SHA, evento);
        verify(valores).set(anyString(), json.capture(), eq(CacheEventosAuditoriaRedis.VIGENCIA));
        when(valores.get(CacheEventosAuditoriaRedis.clave(evento.idEvento(), SHA))).thenReturn(json.getValue());

        assertEquals(evento, cache.leer(evento.idEvento(), SHA).orElseThrow());
    }

    @Test
    void unaClaveAusenteNoDevuelveNada() {
        assertTrue(cache.leer(evento.idEvento(), SHA).isEmpty());
    }

    @Test
    void unaEntradaIlegibleSeDescartaYSeTrataComoAusente() {
        String clave = CacheEventosAuditoriaRedis.clave(evento.idEvento(), SHA);
        when(valores.get(clave)).thenReturn("esto no es json");

        assertTrue(cache.leer(evento.idEvento(), SHA).isEmpty());
        verify(redis).delete(clave);
    }

    @Test
    void siRedisFallaAlDescartarNoSePropagaElError() {
        String clave = CacheEventosAuditoriaRedis.clave(evento.idEvento(), SHA);
        when(valores.get(clave)).thenReturn("esto no es json");
        when(redis.delete(clave)).thenThrow(new IllegalStateException("Redis caído"));

        assertTrue(cache.leer(evento.idEvento(), SHA).isEmpty());
    }

    @Test
    void siRedisNoEstaDisponibleLeerDevuelveVacioYGuardarNoFalla() {
        when(valores.get(anyString())).thenThrow(new IllegalStateException("Redis caído"));
        doThrow(new IllegalStateException("Redis caído")).when(valores).set(anyString(), anyString(), eq(CacheEventosAuditoriaRedis.VIGENCIA));

        assertTrue(cache.leer(evento.idEvento(), SHA).isEmpty());
        cache.guardar(SHA, evento);
    }
}
