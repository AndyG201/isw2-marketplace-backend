package co.edu.unbosque.marketplace.publicacion.domain;


import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RevisionPublicacionTest {

    private RevisionPublicacion revision;
    private UUID idRevision;
    private UUID idPublicacion;
    private UUID idAdministrador;
    private OffsetDateTime fechaRevision;

    @BeforeEach
    void setUp() {
        idRevision = UUID.randomUUID();
        idPublicacion = UUID.randomUUID();
        idAdministrador = UUID.randomUUID();
        fechaRevision = OffsetDateTime.parse("2026-11-18T10:30:00-05:00");

        revision = new RevisionPublicacion();

        revision.setIdRevision(idRevision);
        revision.setIdPublicacion(idPublicacion);
        revision.setIdAdministrador(idAdministrador);
        revision.setAccion("APROBAR");
        revision.setResultado("APROBADA");
        revision.setMotivo("La publicación cumple con los requisitos establecidos.");
        revision.setFechaRevision(fechaRevision);
    }

    @Test
    void constructorVacioDebeCrearObjeto() {
        RevisionPublicacion nueva = new RevisionPublicacion();

        assertNotNull(nueva);
        assertNull(nueva.getIdRevision());
        assertNull(nueva.getIdPublicacion());
        assertNull(nueva.getIdAdministrador());
        assertNull(nueva.getAccion());
        assertNull(nueva.getResultado());
        assertNull(nueva.getMotivo());
        assertNull(nueva.getFechaRevision());
    }

    @Test
    void debeAsignarYConsultarTodosLosAtributos() {
        assertAll(
            () -> assertEquals(idRevision, revision.getIdRevision()),
            () -> assertEquals(idPublicacion, revision.getIdPublicacion()),
            () -> assertEquals(idAdministrador, revision.getIdAdministrador()),
            () -> assertEquals("APROBAR", revision.getAccion()),
            () -> assertEquals("APROBADA", revision.getResultado()),
            () -> assertEquals("La publicación cumple con los requisitos establecidos.", revision.getMotivo()),
            () -> assertEquals(fechaRevision, revision.getFechaRevision())
        );
    }

    @Test
    void debeModificarTodosLosAtributosConSusSetters() {
        UUID nuevoIdRevision = UUID.randomUUID();
        UUID nuevoIdPublicacion = UUID.randomUUID();
        UUID nuevoIdAdministrador = UUID.randomUUID();
        OffsetDateTime nuevaFecha = fechaRevision.plusDays(1);

        revision.setIdRevision(nuevoIdRevision);
        revision.setIdPublicacion(nuevoIdPublicacion);
        revision.setIdAdministrador(nuevoIdAdministrador);
        revision.setAccion("RECHAZAR");
        revision.setResultado("RECHAZADA");
        revision.setMotivo("La publicación no cumple con las políticas.");
        revision.setFechaRevision(nuevaFecha);

        assertAll(
            () -> assertEquals(nuevoIdRevision, revision.getIdRevision()),
            () -> assertEquals(nuevoIdPublicacion, revision.getIdPublicacion()),
            () -> assertEquals(nuevoIdAdministrador, revision.getIdAdministrador()),
            () -> assertEquals("RECHAZAR", revision.getAccion()),
            () -> assertEquals("RECHAZADA", revision.getResultado()),
            () -> assertEquals("La publicación no cumple con las políticas.", revision.getMotivo()),
            () -> assertEquals(nuevaFecha, revision.getFechaRevision())
        );
    }

    @Test
    void debePermitirMotivoNulo() {
        revision.setMotivo(null);

        assertNull(revision.getMotivo());
    }

    @Test
    void debePermitirMotivoVacio() {
        revision.setMotivo("");

        assertEquals("", revision.getMotivo());
    }

    @Test
    void debePermitirMotivosExtensos() {
        String motivo = "La publicación requiere ajustes. ".repeat(100);

        revision.setMotivo(motivo);

        assertEquals(motivo, revision.getMotivo());
    }

    @Test
    void debePermitirValoresNulosEnLosAtributos() {
        revision.setIdRevision(null);
        revision.setIdPublicacion(null);
        revision.setIdAdministrador(null);
        revision.setAccion(null);
        revision.setResultado(null);
        revision.setMotivo(null);
        revision.setFechaRevision(null);

        assertAll(
            () -> assertNull(revision.getIdRevision()),
            () -> assertNull(revision.getIdPublicacion()),
            () -> assertNull(revision.getIdAdministrador()),
            () -> assertNull(revision.getAccion()),
            () -> assertNull(revision.getResultado()),
            () -> assertNull(revision.getMotivo()),
            () -> assertNull(revision.getFechaRevision())
        );
    }

    @Test
    void debeConservarElDesfaseHorarioDeLaFechaRevision() {
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-20T15:45:00+02:00");

        revision.setFechaRevision(fecha);

        assertEquals(fecha, revision.getFechaRevision());
        assertEquals("+02:00", revision.getFechaRevision().getOffset().toString());
    }

    @Test
    void debePermitirCambiarLaAccionYElResultadoIndependientemente() {
        revision.setAccion("SOLICITAR_AJUSTES");

        assertEquals("SOLICITAR_AJUSTES", revision.getAccion());
        assertEquals("APROBADA", revision.getResultado());

        revision.setResultado("PENDIENTE");

        assertEquals("SOLICITAR_AJUSTES", revision.getAccion());
        assertEquals("PENDIENTE", revision.getResultado());
    }
}

