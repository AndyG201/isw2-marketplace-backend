package co.edu.unbosque.marketplace.doble;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.publicacion.application.dto.PaginaPublicacionesResponse;
import co.edu.unbosque.marketplace.publicacion.application.port.CacheBusquedaPublicaciones;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

/** Comportamiento que toda implementación de la caché de búsqueda debe cumplir (Redis y en memoria). */
abstract class ContratoCacheBusquedaTest {

    abstract CacheBusquedaPublicaciones cache();

    private static PublicacionBusquedaCriteria criterio(String ciudad, List<UUID> servicios, int pagina) {
        return new PublicacionBusquedaCriteria(ciudad, LocalDate.of(2030, 1, 10), LocalDate.of(2030, 1, 12), 2, "casa",
                BigDecimal.ONE, BigDecimal.TEN, servicios, pagina, 20);
    }

    private static PaginaPublicacionesResponse pagina(String mensaje) {
        return new PaginaPublicacionesResponse(List.of(), 0, 20, 0, 0, mensaje);
    }

    @Test
    void laVersionEsEstableMientrasNadieInvalida() {
        String version = cache().versionActual();

        assertNotNull(version);
        assertEquals(version, cache().versionActual());
    }

    @Test
    void invalidarCambiaLaVersion() {
        String antes = cache().versionActual();

        cache().invalidar();

        assertNotEquals(antes, cache().versionActual());
    }

    @Test
    void laMismaConsultaDaLaMismaClaveSinImportarElOrdenDeLosServicios() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        String version = cache().versionActual();

        assertEquals(cache().clave(version, criterio("bogota", List.of(a, b), 0)),
                cache().clave(version, criterio("bogota", List.of(b, a), 0)));
    }

    @Test
    void consultasDistintasOVersionesDistintasDanClavesDistintas() {
        String version = cache().versionActual();
        String base = cache().clave(version, criterio("bogota", List.of(), 0));

        assertNotEquals(base, cache().clave(version, criterio("cali", List.of(), 0)));
        assertNotEquals(base, cache().clave(version, criterio("bogota", List.of(), 1)));
        assertNotEquals(base, cache().clave(version, criterio("bogota", List.of(UUID.randomUUID()), 0)));
        assertNotEquals(base, cache().clave(version + "x", criterio("bogota", List.of(), 0)));
    }

    @Test
    void guardaYLeeElMismoResultadoYUnaClaveAusenteDevuelveNulo() {
        String clave = cache().clave(cache().versionActual(), criterio("ciudad-" + UUID.randomUUID(), List.of(), 0));
        assertNull(cache().leer(clave));

        cache().guardar(clave, pagina("hola"));

        assertEquals(pagina("hola"), cache().leer(clave));
    }

    @Test
    void despuesDeInvalidarLaEntradaViejaYaNoSeEncuentraConLaClaveNueva() {
        String ciudad = "ciudad-" + UUID.randomUUID();
        String claveVieja = cache().clave(cache().versionActual(), criterio(ciudad, List.of(), 0));
        cache().guardar(claveVieja, pagina("vieja"));

        cache().invalidar();

        String claveNueva = cache().clave(cache().versionActual(), criterio(ciudad, List.of(), 0));
        assertNotEquals(claveVieja, claveNueva);
        assertNull(cache().leer(claveNueva));
    }
}
