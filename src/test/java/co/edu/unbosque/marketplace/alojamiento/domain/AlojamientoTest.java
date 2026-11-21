package co.edu.unbosque.marketplace.alojamiento.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AlojamientoTest {

    @Test
    void debeCrearAlojamientoConConstructorVacio() {
        Alojamiento alojamiento = new Alojamiento();

        assertNotNull(alojamiento);
        assertNull(alojamiento.getIdAlojamiento());
        assertNull(alojamiento.getIdAnfitrion());
        assertNull(alojamiento.getUbicacion());
        assertNull(alojamiento.getCiudad());
        assertNull(alojamiento.getTipoAlojamiento());
        assertEquals(0, alojamiento.getCapacidad());
        assertNull(alojamiento.getFechaCreacion());
    }

    @Test
    void debeCrearAlojamientoConTodosLosAtributos() {
        UUID idAlojamiento = UUID.randomUUID();
        UUID idAnfitrion = UUID.randomUUID();
        OffsetDateTime fecha = OffsetDateTime.now();

        Alojamiento alojamiento = new Alojamiento(
                idAlojamiento,
                idAnfitrion,
                "Calle 100 #15-20",
                "Bogotá",
                "Apartamento",
                4,
                fecha
        );

        assertEquals(idAlojamiento, alojamiento.getIdAlojamiento());
        assertEquals(idAnfitrion, alojamiento.getIdAnfitrion());
        assertEquals("Calle 100 #15-20", alojamiento.getUbicacion());
        assertEquals("Bogotá", alojamiento.getCiudad());
        assertEquals("Apartamento", alojamiento.getTipoAlojamiento());
        assertEquals(4, alojamiento.getCapacidad());
        assertEquals(fecha, alojamiento.getFechaCreacion());
    }

    @Test
    void debeActualizarIdAlojamiento() {
        Alojamiento alojamiento = new Alojamiento();
        UUID id = UUID.randomUUID();

        alojamiento.setIdAlojamiento(id);

        assertEquals(id, alojamiento.getIdAlojamiento());
    }

    @Test
    void debeActualizarIdAnfitrion() {
        Alojamiento alojamiento = new Alojamiento();
        UUID id = UUID.randomUUID();

        alojamiento.setIdAnfitrion(id);

        assertEquals(id, alojamiento.getIdAnfitrion());
    }

    @Test
    void debeActualizarUbicacion() {
        Alojamiento alojamiento = new Alojamiento();

        alojamiento.setUbicacion("Carrera 7 #20-30");

        assertEquals("Carrera 7 #20-30", alojamiento.getUbicacion());
    }

    @Test
    void debeActualizarCiudad() {
        Alojamiento alojamiento = new Alojamiento();

        alojamiento.setCiudad("Medellín");

        assertEquals("Medellín", alojamiento.getCiudad());
    }

    @Test
    void debeActualizarTipoAlojamiento() {
        Alojamiento alojamiento = new Alojamiento();

        alojamiento.setTipoAlojamiento("Casa");

        assertEquals("Casa", alojamiento.getTipoAlojamiento());
    }

    @Test
    void debeActualizarCapacidad() {
        Alojamiento alojamiento = new Alojamiento();

        alojamiento.setCapacidad(6);

        assertEquals(6, alojamiento.getCapacidad());
    }

    @Test
    void debeActualizarFechaCreacion() {
        Alojamiento alojamiento = new Alojamiento();
        OffsetDateTime fecha = OffsetDateTime.now();

        alojamiento.setFechaCreacion(fecha);

        assertEquals(fecha, alojamiento.getFechaCreacion());
    }

    @Test
    void debePermitirActualizarAtributosConValoresNulos() {
        Alojamiento alojamiento = new Alojamiento(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Bogotá",
                "Bogotá",
                "Apartamento",
                4,
                OffsetDateTime.now()
        );

        alojamiento.setIdAlojamiento(null);
        alojamiento.setIdAnfitrion(null);
        alojamiento.setUbicacion(null);
        alojamiento.setCiudad(null);
        alojamiento.setTipoAlojamiento(null);
        alojamiento.setFechaCreacion(null);

        assertNull(alojamiento.getIdAlojamiento());
        assertNull(alojamiento.getIdAnfitrion());
        assertNull(alojamiento.getUbicacion());
        assertNull(alojamiento.getCiudad());
        assertNull(alojamiento.getTipoAlojamiento());
        assertNull(alojamiento.getFechaCreacion());
    }
}
