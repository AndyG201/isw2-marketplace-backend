package co.edu.unbosque.marketplace.disponibilidad.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NocheReservadaTest {

    private NocheReservada noche;
    private UUID idNoche;
    private UUID idAlojamiento;
    private LocalDate fecha;

    @BeforeEach
    void setUp() {
        idNoche = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        fecha = LocalDate.of(2026, 11, 20);

        noche = new NocheReservada();
    }

    @Test
    void constructorVacio() {
        assertNotNull(noche);
        assertNull(noche.getIdNoche());
        assertNull(noche.getIdAlojamiento());
        assertNull(noche.getFecha());
    }

    @Test
    void constructorConParametros() {
        noche = new NocheReservada(idNoche, idAlojamiento, fecha);

        assertEquals(idNoche, noche.getIdNoche());
        assertEquals(idAlojamiento, noche.getIdAlojamiento());
        assertEquals(fecha, noche.getFecha());
    }

    @Test
    void obtenerIdNoche() {
        noche.setIdNoche(idNoche);

        assertEquals(idNoche, noche.getIdNoche());
    }

    @Test
    void asignarIdNoche() {
        noche.setIdNoche(idNoche);

        assertNotNull(noche.getIdNoche());
    }

    @Test
    void obtenerIdAlojamiento() {
        noche.setIdAlojamiento(idAlojamiento);

        assertEquals(idAlojamiento, noche.getIdAlojamiento());
    }

    @Test
    void asignarIdAlojamiento() {
        noche.setIdAlojamiento(idAlojamiento);

        assertNotNull(noche.getIdAlojamiento());
    }

    @Test
    void obtenerFecha() {
        noche.setFecha(fecha);

        assertEquals(fecha, noche.getFecha());
    }

    @Test
    void asignarFecha() {
        noche.setFecha(fecha);

        assertNotNull(noche.getFecha());
    }

    @Test
    void actualizarAtributos() {
        UUID nuevoIdNoche = UUID.randomUUID();
        UUID nuevoIdAlojamiento = UUID.randomUUID();
        LocalDate nuevaFecha = LocalDate.of(2026, 12, 15);

        noche.setIdNoche(nuevoIdNoche);
        noche.setIdAlojamiento(nuevoIdAlojamiento);
        noche.setFecha(nuevaFecha);

        assertEquals(nuevoIdNoche, noche.getIdNoche());
        assertEquals(nuevoIdAlojamiento, noche.getIdAlojamiento());
        assertEquals(nuevaFecha, noche.getFecha());
    }
}
