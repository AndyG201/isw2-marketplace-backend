package co.edu.unbosque.marketplace.cancelacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PoliticaCancelacionTest {

    private PoliticaCancelacion politica;

    private UUID idPolitica;
    private BigDecimal porcentajeDevolucion;

    @BeforeEach
    void setUp() {
        idPolitica = UUID.randomUUID();
        porcentajeDevolucion = new BigDecimal("100.00");

        politica = new PoliticaCancelacion();
        politica.setIdPolitica(idPolitica);
        politica.setNombre("Flexible");
        politica.setVersion(1);
        politica.setDescripcion("Politica de cancelacion flexible");
        politica.setAnticipacionHoras(48);
        politica.setPorcentajeDevolucion(porcentajeDevolucion);
        politica.setActiva(true);
    }

    @Test
    void constructorVacioCreaPolitica() {
        PoliticaCancelacion nuevaPolitica = new PoliticaCancelacion();

        assertNotNull(nuevaPolitica);
    }

    @Test
    void constructorVacioInicializaPoliticaActiva() {
        PoliticaCancelacion nuevaPolitica = new PoliticaCancelacion();

        assertTrue(nuevaPolitica.isActiva());
    }

    @Test
    void gettersRetornanLosValoresAsignados() {
        assertEquals(idPolitica, politica.getIdPolitica());
        assertEquals("Flexible", politica.getNombre());
        assertEquals(1, politica.getVersion());
        assertEquals("Politica de cancelacion flexible", politica.getDescripcion());
        assertEquals(48, politica.getAnticipacionHoras());
        assertEquals(0, porcentajeDevolucion.compareTo(
                politica.getPorcentajeDevolucion()));
        assertTrue(politica.isActiva());
    }

    @Test
    void settersActualizanTodosLosAtributos() {
        UUID nuevoId = UUID.randomUUID();
        BigDecimal nuevoPorcentaje = new BigDecimal("75.50");

        politica.setIdPolitica(nuevoId);
        politica.setNombre("Moderada");
        politica.setVersion(2);
        politica.setDescripcion("Politica de cancelacion moderada");
        politica.setAnticipacionHoras(24);
        politica.setPorcentajeDevolucion(nuevoPorcentaje);
        politica.setActiva(false);

        assertEquals(nuevoId, politica.getIdPolitica());
        assertEquals("Moderada", politica.getNombre());
        assertEquals(2, politica.getVersion());
        assertEquals("Politica de cancelacion moderada", politica.getDescripcion());
        assertEquals(24, politica.getAnticipacionHoras());
        assertEquals(0, nuevoPorcentaje.compareTo(
                politica.getPorcentajeDevolucion()));
        assertFalse(politica.isActiva());
    }

    @Test
    void settersPermitenAsignarValoresNulos() {
        politica.setIdPolitica(null);
        politica.setNombre(null);
        politica.setDescripcion(null);
        politica.setAnticipacionHoras(null);
        politica.setPorcentajeDevolucion(null);

        assertNull(politica.getIdPolitica());
        assertNull(politica.getNombre());
        assertNull(politica.getDescripcion());
        assertNull(politica.getAnticipacionHoras());
        assertNull(politica.getPorcentajeDevolucion());
    }

    @Test
    void setterPermiteCambiarEstadoActivo() {
        politica.setActiva(false);

        assertFalse(politica.isActiva());

        politica.setActiva(true);

        assertTrue(politica.isActiva());
    }
}