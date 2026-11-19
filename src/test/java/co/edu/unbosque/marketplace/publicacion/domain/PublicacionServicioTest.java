package co.edu.unbosque.marketplace.publicacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicacionServicioTest {

    private PublicacionServicio publicacionServicio;
    private UUID idPublicacion;
    private UUID idServicio;

    @BeforeEach
    void setUp() {
        idPublicacion = UUID.randomUUID();
        idServicio = UUID.randomUUID();

        publicacionServicio = new PublicacionServicio(
            idPublicacion,
            idServicio
        );
    }

    @Test
    void constructorVacioDebeCrearObjeto() {
        PublicacionServicio nueva = new PublicacionServicio();

        assertNotNull(nueva);
        assertNull(nueva.getIdPublicacion());
        assertNull(nueva.getIdServicio());
    }

    @Test
    void constructorCompletoDebeAsignarLosAtributos() {
        assertAll(
            () -> assertEquals(idPublicacion, publicacionServicio.getIdPublicacion()),
            () -> assertEquals(idServicio, publicacionServicio.getIdServicio())
        );
    }

    @Test
    void debeModificarLosAtributosConSusSetters() {
        UUID nuevoIdPublicacion = UUID.randomUUID();
        UUID nuevoIdServicio = UUID.randomUUID();

        publicacionServicio.setIdPublicacion(nuevoIdPublicacion);
        publicacionServicio.setIdServicio(nuevoIdServicio);

        assertAll(
            () -> assertEquals(nuevoIdPublicacion, publicacionServicio.getIdPublicacion()),
            () -> assertEquals(nuevoIdServicio, publicacionServicio.getIdServicio())
        );
    }

    @Test
    void debePermitirValoresNulosEnLosAtributos() {
        publicacionServicio.setIdPublicacion(null);
        publicacionServicio.setIdServicio(null);

        assertAll(
            () -> assertNull(publicacionServicio.getIdPublicacion()),
            () -> assertNull(publicacionServicio.getIdServicio())
        );
    }

    @Test
    void debePermitirActualizarUnSoloIdentificador() {
        UUID nuevoIdPublicacion = UUID.randomUUID();

        publicacionServicio.setIdPublicacion(nuevoIdPublicacion);

        assertEquals(nuevoIdPublicacion, publicacionServicio.getIdPublicacion());
        assertEquals(idServicio, publicacionServicio.getIdServicio());
    }

    @Test
    void debeConservarLosIdentificadoresIndependientemente() {
        UUID otroIdPublicacion = UUID.randomUUID();
        UUID otroIdServicio = UUID.randomUUID();

        PublicacionServicio otro = new PublicacionServicio(
            otroIdPublicacion,
            otroIdServicio
        );

        assertNotEquals(publicacionServicio.getIdPublicacion(), otro.getIdPublicacion());
        assertNotEquals(publicacionServicio.getIdServicio(), otro.getIdServicio());
    }
}
