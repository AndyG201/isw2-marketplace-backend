package co.edu.unbosque.marketplace.publicacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicacionServicioIdTest {

    private PublicacionServicioId id;
    private UUID idPublicacion;
    private UUID idServicio;

    @BeforeEach
    void setUp() {
        idPublicacion = UUID.randomUUID();
        idServicio = UUID.randomUUID();

        id = new PublicacionServicioId(idPublicacion, idServicio);
    }

    @Test
    void constructorVacioDebeCrearObjeto() {
        PublicacionServicioId nuevo = new PublicacionServicioId();

        assertNotNull(nuevo);
        assertNull(nuevo.getIdPublicacion());
        assertNull(nuevo.getIdServicio());
    }

    @Test
    void constructorCompletoDebeAsignarLosAtributos() {
        assertAll(
            () -> assertEquals(idPublicacion, id.getIdPublicacion()),
            () -> assertEquals(idServicio, id.getIdServicio())
        );
    }

    @Test
    void debeModificarLosAtributosConSusSetters() {
        UUID nuevoIdPublicacion = UUID.randomUUID();
        UUID nuevoIdServicio = UUID.randomUUID();

        id.setIdPublicacion(nuevoIdPublicacion);
        id.setIdServicio(nuevoIdServicio);

        assertAll(
            () -> assertEquals(nuevoIdPublicacion, id.getIdPublicacion()),
            () -> assertEquals(nuevoIdServicio, id.getIdServicio())
        );
    }

    @Test
    void dosObjetosConLosMismosIdentificadoresDebenSerIguales() {
        PublicacionServicioId otro = new PublicacionServicioId(
            idPublicacion,
            idServicio
        );

        assertEquals(id, otro);
        assertEquals(otro, id);
    }

    @Test
    void dosObjetosConIdentificadoresDiferentesNoDebenSerIguales() {
        PublicacionServicioId otro = new PublicacionServicioId(
            UUID.randomUUID(),
            UUID.randomUUID()
        );

        assertNotEquals(id, otro);
    }

    @Test
    void debeDiferenciarseCuandoCambiaElIdPublicacion() {
        PublicacionServicioId otro = new PublicacionServicioId(
            UUID.randomUUID(),
            idServicio
        );

        assertNotEquals(id, otro);
    }

    @Test
    void debeDiferenciarseCuandoCambiaElIdServicio() {
        PublicacionServicioId otro = new PublicacionServicioId(
            idPublicacion,
            UUID.randomUUID()
        );

        assertNotEquals(id, otro);
    }

    @Test
    void debeRetornarTrueAlCompararseConsigoMismo() {
        assertEquals(id, id);
    }

    @Test
    void debeRetornarFalseAlCompararseConNull() {
        assertNotEquals(null, id);
    }

    @Test
    void debeRetornarFalseAlCompararseConOtroTipoDeObjeto() {
        assertNotEquals("PublicacionServicioId", id);
    }

    @Test
    void objetosIgualesDebenTenerElMismoHashCode() {
        PublicacionServicioId otro = new PublicacionServicioId(
            idPublicacion,
            idServicio
        );

        assertEquals(id.hashCode(), otro.hashCode());
    }

    @Test
    void debePermitirIdentificadoresNulos() {
        PublicacionServicioId nuevo = new PublicacionServicioId(null, null);

        assertNull(nuevo.getIdPublicacion());
        assertNull(nuevo.getIdServicio());

        PublicacionServicioId otro = new PublicacionServicioId(null, null);

        assertEquals(nuevo, otro);
        assertEquals(nuevo.hashCode(), otro.hashCode());
    }

    @Test
    void debeMantenerLaIgualdadDespuesDeModificarLosIdentificadores() {
        PublicacionServicioId otro = new PublicacionServicioId();

        otro.setIdPublicacion(idPublicacion);
        otro.setIdServicio(idServicio);

        assertEquals(id, otro);
        assertEquals(id.hashCode(), otro.hashCode());
    }
}
