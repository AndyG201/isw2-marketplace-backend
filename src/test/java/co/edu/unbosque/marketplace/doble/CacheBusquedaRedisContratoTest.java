package co.edu.unbosque.marketplace.doble;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.publicacion.application.port.CacheBusquedaPublicaciones;

/** El mismo contrato contra la implementación real, con Redis. */
@SpringBootTest
@ActiveProfiles("test")
class CacheBusquedaRedisContratoTest extends ContratoCacheBusquedaTest {

    @Autowired
    private CacheBusquedaPublicaciones cache;

    @Override
    CacheBusquedaPublicaciones cache() {
        return cache;
    }
}
