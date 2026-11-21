package co.edu.unbosque.marketplace.disponibilidad.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PeriodoDisponibilidadTest {

    private PeriodoDisponibilidad periodo;
    private UUID idPeriodo;
    private UUID idAlojamiento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private OffsetDateTime fechaCreacion;

    @BeforeEach
    void setUp() {
        idPeriodo = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        fechaInicio = LocalDate.of(2026, 11, 20);
        fechaFin = LocalDate.of(2026, 11, 30);
        fechaCreacion = OffsetDateTime.parse("2026-11-20T10:30:00-05:00");

        periodo = new PeriodoDisponibilidad();
    }

    @Test
    void constructorVacio() {
        assertNotNull(periodo);
        assertNull(periodo.getIdPeriodo());
        assertNull(periodo.getIdAlojamiento());
        assertNull(periodo.getFechaInicio());
        assertNull(periodo.getFechaFin());
        assertFalse(periodo.isHabilitado());
        assertNull(periodo.getFechaCreacion());
    }

    @Test
    void constructorConParametros() {
        periodo = new PeriodoDisponibilidad(
                idPeriodo,
                idAlojamiento,
                fechaInicio,
                fechaFin,
                true,
                fechaCreacion
        );

        assertEquals(idPeriodo, periodo.getIdPeriodo());
        assertEquals(idAlojamiento, periodo.getIdAlojamiento());
        assertEquals(fechaInicio, periodo.getFechaInicio());
        assertEquals(fechaFin, periodo.getFechaFin());
        assertTrue(periodo.isHabilitado());
        assertEquals(fechaCreacion, periodo.getFechaCreacion());
    }

    @Test
    void obtenerIdPeriodo() {
        periodo.setIdPeriodo(idPeriodo);

        assertEquals(idPeriodo, periodo.getIdPeriodo());
    }

    @Test
    void asignarIdPeriodo() {
        periodo.setIdPeriodo(idPeriodo);

        assertNotNull(periodo.getIdPeriodo());
    }

    @Test
    void obtenerIdAlojamiento() {
        periodo.setIdAlojamiento(idAlojamiento);

        assertEquals(idAlojamiento, periodo.getIdAlojamiento());
    }

    @Test
    void asignarIdAlojamiento() {
        periodo.setIdAlojamiento(idAlojamiento);

        assertNotNull(periodo.getIdAlojamiento());
    }

    @Test
    void obtenerFechaInicio() {
        periodo.setFechaInicio(fechaInicio);

        assertEquals(fechaInicio, periodo.getFechaInicio());
    }

    @Test
    void asignarFechaInicio() {
        periodo.setFechaInicio(fechaInicio);

        assertNotNull(periodo.getFechaInicio());
    }

    @Test
    void obtenerFechaFin() {
        periodo.setFechaFin(fechaFin);

        assertEquals(fechaFin, periodo.getFechaFin());
    }

    @Test
    void asignarFechaFin() {
        periodo.setFechaFin(fechaFin);

        assertNotNull(periodo.getFechaFin());
    }

    @Test
    void obtenerHabilitado() {
        periodo.setHabilitado(true);

        assertTrue(periodo.isHabilitado());
    }

    @Test
    void asignarHabilitado() {
        periodo.setHabilitado(false);

        assertFalse(periodo.isHabilitado());
    }

    @Test
    void obtenerFechaCreacion() {
        periodo.setFechaCreacion(fechaCreacion);

        assertEquals(fechaCreacion, periodo.getFechaCreacion());
    }

    @Test
    void asignarFechaCreacion() {
        periodo.setFechaCreacion(fechaCreacion);

        assertNotNull(periodo.getFechaCreacion());
    }

    @Test
    void actualizarTodosLosAtributos() {
        UUID nuevoIdPeriodo = UUID.randomUUID();
        UUID nuevoIdAlojamiento = UUID.randomUUID();
        LocalDate nuevaFechaInicio = LocalDate.of(2026, 12, 1);
        LocalDate nuevaFechaFin = LocalDate.of(2026, 12, 15);
        OffsetDateTime nuevaFechaCreacion =
                OffsetDateTime.parse("2026-12-01T14:00:00-05:00");

        periodo.setIdPeriodo(nuevoIdPeriodo);
        periodo.setIdAlojamiento(nuevoIdAlojamiento);
        periodo.setFechaInicio(nuevaFechaInicio);
        periodo.setFechaFin(nuevaFechaFin);
        periodo.setHabilitado(true);
        periodo.setFechaCreacion(nuevaFechaCreacion);

        assertEquals(nuevoIdPeriodo, periodo.getIdPeriodo());
        assertEquals(nuevoIdAlojamiento, periodo.getIdAlojamiento());
        assertEquals(nuevaFechaInicio, periodo.getFechaInicio());
        assertEquals(nuevaFechaFin, periodo.getFechaFin());
        assertTrue(periodo.isHabilitado());
        assertEquals(nuevaFechaCreacion, periodo.getFechaCreacion());
    }
}
