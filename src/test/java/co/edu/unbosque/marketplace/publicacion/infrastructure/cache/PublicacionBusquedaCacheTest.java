package co.edu.unbosque.marketplace.publicacion.infrastructure.cache;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.marketplace.publicacion.application.dto.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

/** Caché de búsqueda: aciertos, fallos, versión de invalidación y respaldo cuando Redis no responde. */
class PublicacionBusquedaCacheTest {

    private static final String VERSION_KEY = "marketplace:publicaciones:busqueda:version";

    private StringRedisTemplate redis;
    private ValueOperations<String, String> valores;
    private PublicacionBusquedaCache cache;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void preparar() {
        redis = mock(StringRedisTemplate.class);
        valores = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(valores);
        cache = new PublicacionBusquedaCache(redis, new ObjectMapper().findAndRegisterModules());
    }

    private static PublicacionBusquedaCriteria criterio(String ciudad, List<UUID> servicios, int pagina) {
        return new PublicacionBusquedaCriteria(ciudad, LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 12), 2, "casa",
                BigDecimal.ONE, BigDecimal.TEN, servicios, pagina, 20);
    }

    private static PaginaPublicacionesResponse pagina() {
        return new PaginaPublicacionesResponse(List.of(), 0, 20, 0, 0, "Sin resultados");
    }

    // ---------- versión ----------

    @Test
    void laVersionExistenteSeDevuelveTalCual() {
        when(valores.get(VERSION_KEY)).thenReturn("7");

        assertEquals("7", cache.versionActual());
        verify(valores, never()).setIfAbsent(anyString(), anyString());
    }

    @Test
    void siNoHayVersionSeInicializaEnCero() {
        when(valores.get(VERSION_KEY)).thenReturn(null, "0");

        assertEquals("0", cache.versionActual());
        verify(valores).setIfAbsent(VERSION_KEY, "0");
    }

    @Test
    void sinRedisLaVersionEsNulaYLaBusquedaVaALaBaseDeDatos() {
        when(valores.get(VERSION_KEY)).thenThrow(new IllegalStateException("Redis caído"));

        assertNull(cache.versionActual());
    }

    // ---------- clave ----------

    @Test
    void laClaveEsEstableYDependeDeLaVersionYDeLosFiltros() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();

        String clave = cache.clave("3", criterio("bogotá", List.of(a, b), 0));

        assertEquals(clave, cache.clave("3", criterio("bogotá", List.of(a, b), 0)));
        assertTrue(clave.startsWith("marketplace:publicaciones:busqueda:3:"));
        assertNotEquals(clave, cache.clave("4", criterio("bogotá", List.of(a, b), 0)));
        assertNotEquals(clave, cache.clave("3", criterio("cali", List.of(a, b), 0)));
        assertNotEquals(clave, cache.clave("3", criterio("bogotá", List.of(a, b), 1)));
    }

    @Test
    void elOrdenDeLosServiciosNoCambiaLaClave() {
        UUID a = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID b = UUID.fromString("00000000-0000-0000-0000-000000000002");

        assertEquals(cache.clave("1", criterio("bogotá", List.of(a, b), 0)),
                cache.clave("1", criterio("bogotá", List.of(b, a), 0)));
    }

    @Test
    void losFiltrosAusentesSeTratanComoVacios() {
        PublicacionBusquedaCriteria sinFiltros = new PublicacionBusquedaCriteria(
                null, null, null, null, null, null, null, List.of(), 0, 20);

        assertNotNull(cache.clave("1", sinFiltros));
    }

    // ---------- leer ----------

    @Test
    void unAciertoDevuelveLaPaginaGuardada() throws Exception {
        when(valores.get("k")).thenReturn(new ObjectMapper().writeValueAsString(pagina()));

        PaginaPublicacionesResponse leida = cache.leer("k");

        assertEquals("Sin resultados", leida.mensaje());
        assertEquals(20, leida.tamano());
    }

    @Test
    void unFalloDeCacheDevuelveNulo() {
        when(valores.get("k")).thenReturn(null);

        assertNull(cache.leer("k"));
    }

    @Test
    void unaEntradaCorruptaSeBorraYSeIgnora() {
        when(valores.get("k")).thenReturn("{no es json");

        assertNull(cache.leer("k"));
        verify(redis).delete("k");
    }

    @Test
    void unaEntradaCorruptaSeIgnoraAunqueNoSePuedaBorrar() {
        when(valores.get("k")).thenReturn("{no es json");
        when(redis.delete("k")).thenThrow(new IllegalStateException("Redis caído"));

        assertNull(assertDoesNotThrow(() -> cache.leer("k")));
    }

    @Test
    void sinRedisLeerDevuelveNulo() {
        when(valores.get("k")).thenThrow(new IllegalStateException("Redis caído"));

        assertNull(cache.leer("k"));
    }

    // ---------- guardar ----------

    @Test
    void guardarEscribeElJsonConUnaVigenciaDe60Segundos() {
        cache.guardar("k", pagina());

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(valores).set(eq("k"), json.capture(), eq(Duration.ofSeconds(60)));
        assertTrue(json.getValue().contains("Sin resultados"));
    }

    @Test
    void sinRedisGuardarNoFalla() {
        when(redis.opsForValue()).thenThrow(new IllegalStateException("Redis caído"));

        assertDoesNotThrow(() -> cache.guardar("k", pagina()));
    }

    // ---------- invalidar ----------

    @Test
    void invalidarSubeLaVersionParaDescartarLasEntradasAnteriores() {
        cache.invalidar();

        verify(valores).increment(VERSION_KEY);
    }

    @Test
    void sinRedisInvalidarNoFalla() {
        when(valores.increment(VERSION_KEY)).thenThrow(new IllegalStateException("Redis caído"));

        assertDoesNotThrow(() -> cache.invalidar());
    }
}
