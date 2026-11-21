package co.edu.unbosque.marketplace.cotizacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CotizacionAdicionalTest {

    @Test
    void constructorVacio() {
        CotizacionAdicional adicional = new CotizacionAdicional();

        assertNotNull(adicional);
        assertNull(adicional.getIdCotizacion());
        assertNull(adicional.getIdAdicional());
        assertEquals(0, adicional.getCantidad());
        assertNull(adicional.getPrecioAplicado());
    }

    @Test
    void constructorCompleto() {
        UUID idCotizacion = UUID.randomUUID();
        UUID idAdicional = UUID.randomUUID();
        BigDecimal precioAplicado = new BigDecimal("35000.00");

        CotizacionAdicional adicional = new CotizacionAdicional(
                idCotizacion,
                idAdicional,
                2,
                precioAplicado
        );

        assertEquals(idCotizacion, adicional.getIdCotizacion());
        assertEquals(idAdicional, adicional.getIdAdicional());
        assertEquals(2, adicional.getCantidad());
        assertEquals(precioAplicado, adicional.getPrecioAplicado());
    }

    @Test
    void gettersRetornanLosValoresAsignados() {
        CotizacionAdicional adicional = crearCotizacionAdicional();

        assertNotNull(adicional.getIdCotizacion());
        assertNotNull(adicional.getIdAdicional());
        assertEquals(3, adicional.getCantidad());
        assertEquals(new BigDecimal("45000.00"), adicional.getPrecioAplicado());
    }

    @Test
    void settersActualizanLosAtributos() {
        CotizacionAdicional adicional = new CotizacionAdicional();

        UUID idCotizacion = UUID.randomUUID();
        UUID idAdicional = UUID.randomUUID();
        BigDecimal precioAplicado = new BigDecimal("60000.00");

        adicional.setIdCotizacion(idCotizacion);
        adicional.setIdAdicional(idAdicional);
        adicional.setCantidad(4);
        adicional.setPrecioAplicado(precioAplicado);

        assertEquals(idCotizacion, adicional.getIdCotizacion());
        assertEquals(idAdicional, adicional.getIdAdicional());
        assertEquals(4, adicional.getCantidad());
        assertEquals(precioAplicado, adicional.getPrecioAplicado());
    }

    @Test
    void settersPermitenValoresNulosEnAtributosDeReferencia() {
        CotizacionAdicional adicional = crearCotizacionAdicional();

        adicional.setIdCotizacion(null);
        adicional.setIdAdicional(null);
        adicional.setPrecioAplicado(null);

        assertNull(adicional.getIdCotizacion());
        assertNull(adicional.getIdAdicional());
        assertNull(adicional.getPrecioAplicado());
    }

    private CotizacionAdicional crearCotizacionAdicional() {
        return new CotizacionAdicional(
                UUID.randomUUID(),
                UUID.randomUUID(),
                3,
                new BigDecimal("45000.00")
        );
    }
}
