package co.edu.unbosque.marketplace.doble;


import co.edu.unbosque.marketplace.publicacion.application.port.CacheBusquedaPublicaciones;

class CacheBusquedaEnMemoriaContratoTest extends ContratoCacheBusquedaTest {

    private final CacheBusquedaPublicaciones cache = new CacheBusquedaEnMemoria();

    @Override
    CacheBusquedaPublicaciones cache() {
        return cache;
    }
}
