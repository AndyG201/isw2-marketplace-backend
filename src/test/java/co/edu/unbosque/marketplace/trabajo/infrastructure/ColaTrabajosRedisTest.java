package co.edu.unbosque.marketplace.trabajo.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import co.edu.unbosque.marketplace.trabajo.ColaTrabajos.Trabajo;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

class ColaTrabajosRedisTest {

    private ZSetOperations<String, String> conjunto;
    private ColaTrabajosRedis cola;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void preparar() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        conjunto = mock(ZSetOperations.class);
        when(redis.opsForZSet()).thenReturn(conjunto);
        cola = new ColaTrabajosRedis(redis);
    }

    @Test
    void encolaConUnaPuntuacionQueRespetaLaPrioridad() {
        ArgumentCaptor<Double> puntuacion = ArgumentCaptor.forClass(Double.class);

        cola.encolar(TipoTrabajo.CONCILIAR_PAGOS, "pendientes");
        cola.encolar(TipoTrabajo.EXPORTAR_REPORTE, "reporte-1");

        verify(conjunto).addIfAbsent(eq(ColaTrabajosRedis.CLAVE), eq("CONCILIAR_PAGOS|pendientes"),
                puntuacion.capture());
        verify(conjunto).addIfAbsent(eq(ColaTrabajosRedis.CLAVE), eq("EXPORTAR_REPORTE|reporte-1"),
                puntuacion.capture());
        assertTrue(puntuacion.getAllValues().get(0) < puntuacion.getAllValues().get(1));
    }

    @Test
    void tomaElSiguienteTrabajoDeLaCola() {
        when(conjunto.popMin(ColaTrabajosRedis.CLAVE))
                .thenReturn(new DefaultTypedTuple<>("EXPORTAR_REPORTE|reporte-1", 1d));

        Trabajo trabajo = cola.tomarSiguiente().orElseThrow();

        assertEquals(TipoTrabajo.EXPORTAR_REPORTE, trabajo.tipo());
        assertEquals("reporte-1", trabajo.identificador());
    }

    @Test
    void ignoraUnaColaVaciaOUnMiembroInvalido() {
        when(conjunto.popMin(ColaTrabajosRedis.CLAVE)).thenReturn(null);
        assertTrue(cola.tomarSiguiente().isEmpty());

        when(conjunto.popMin(ColaTrabajosRedis.CLAVE)).thenReturn(new DefaultTypedTuple<>("sin-separador", 1d));
        assertTrue(cola.tomarSiguiente().isEmpty());

        when(conjunto.popMin(ColaTrabajosRedis.CLAVE)).thenReturn(new DefaultTypedTuple<>("DESCONOCIDO|1", 1d));
        assertTrue(cola.tomarSiguiente().isEmpty());
    }

    @Test
    void noEncolaDosVecesElMismoTrabajo() {
        cola.encolar(TipoTrabajo.COPIAR_AUDITORIA, "evento-1");

        verify(conjunto).addIfAbsent(eq(ColaTrabajosRedis.CLAVE), eq("COPIAR_AUDITORIA|evento-1"), anyDouble());
    }
}
