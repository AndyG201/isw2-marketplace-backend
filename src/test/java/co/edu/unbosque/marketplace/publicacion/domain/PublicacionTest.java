package co.edu.unbosque.marketplace.publicacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublicacionTest {

    private Publicacion publicacion;
    private UUID idPublicacion;
    private UUID idAlojamiento;
    private UUID idPolitica;
    private OffsetDateTime fechaCreacion;
    private OffsetDateTime fechaActualizacion;

    @BeforeEach
    void setUp() {
        idPublicacion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        idPolitica = UUID.randomUUID();
        fechaCreacion = OffsetDateTime.parse("2026-11-18T10:00:00-05:00");
        fechaActualizacion = OffsetDateTime.parse("2026-11-18T12:00:00-05:00");

        publicacion = new Publicacion(
            idPublicacion,
            idAlojamiento,
            idPolitica,
            "Apartamento moderno",
            "Apartamento con dos habitaciones y cocina equipada",
            "No se permiten mascotas ni fumar",
            new BigDecimal("250000.00"),
            new BigDecimal("50000.00"),
            "INMEDIATA",
            "ACTIVA",
            fechaCreacion,
            fechaActualizacion
        );
    }

    @Test
    void constructorVacioDebeCrearObjeto() {
        Publicacion nueva = new Publicacion();

        assertNotNull(nueva);
        assertNull(nueva.getIdPublicacion());
        assertNull(nueva.getIdAlojamiento());
        assertNull(nueva.getIdPolitica());
        assertNull(nueva.getTitulo());
        assertNull(nueva.getDescripcion());
        assertNull(nueva.getReglas());
        assertNull(nueva.getPrecioPorNoche());
        assertNull(nueva.getTarifaLimpieza());
        assertNull(nueva.getModoAceptacion());
        assertNull(nueva.getEstado());
        assertNull(nueva.getFechaCreacion());
        assertNull(nueva.getFechaActualizacion());
    }

    @Test
    void constructorCompletoDebeAsignarTodosLosAtributos() {
        assertAll(
            () -> assertEquals(idPublicacion, publicacion.getIdPublicacion()),
            () -> assertEquals(idAlojamiento, publicacion.getIdAlojamiento()),
            () -> assertEquals(idPolitica, publicacion.getIdPolitica()),
            () -> assertEquals("Apartamento moderno", publicacion.getTitulo()),
            () -> assertEquals("Apartamento con dos habitaciones y cocina equipada", publicacion.getDescripcion()),
            () -> assertEquals("No se permiten mascotas ni fumar", publicacion.getReglas()),
            () -> assertEquals(new BigDecimal("250000.00"), publicacion.getPrecioPorNoche()),
            () -> assertEquals(new BigDecimal("50000.00"), publicacion.getTarifaLimpieza()),
            () -> assertEquals("INMEDIATA", publicacion.getModoAceptacion()),
            () -> assertEquals("ACTIVA", publicacion.getEstado()),
            () -> assertEquals(fechaCreacion, publicacion.getFechaCreacion()),
            () -> assertEquals(fechaActualizacion, publicacion.getFechaActualizacion())
        );
    }

    @Test
    void debeModificarTodosLosAtributosConSusSetters() {
        UUID nuevoIdPublicacion = UUID.randomUUID();
        UUID nuevoIdAlojamiento = UUID.randomUUID();
        UUID nuevoIdPolitica = UUID.randomUUID();
        OffsetDateTime nuevaFechaCreacion = fechaCreacion.plusDays(1);
        OffsetDateTime nuevaFechaActualizacion = fechaActualizacion.plusDays(2);

        publicacion.setIdPublicacion(nuevoIdPublicacion);
        publicacion.setIdAlojamiento(nuevoIdAlojamiento);
        publicacion.setIdPolitica(nuevoIdPolitica);
        publicacion.setTitulo("Casa campestre");
        publicacion.setDescripcion("Casa con jardín y terraza");
        publicacion.setReglas("Respetar los horarios de descanso");
        publicacion.setPrecioPorNoche(new BigDecimal("350000.00"));
        publicacion.setTarifaLimpieza(new BigDecimal("70000.00"));
        publicacion.setModoAceptacion("APROBACION");
        publicacion.setEstado("INACTIVA");
        publicacion.setFechaCreacion(nuevaFechaCreacion);
        publicacion.setFechaActualizacion(nuevaFechaActualizacion);

        assertAll(
            () -> assertEquals(nuevoIdPublicacion, publicacion.getIdPublicacion()),
            () -> assertEquals(nuevoIdAlojamiento, publicacion.getIdAlojamiento()),
            () -> assertEquals(nuevoIdPolitica, publicacion.getIdPolitica()),
            () -> assertEquals("Casa campestre", publicacion.getTitulo()),
            () -> assertEquals("Casa con jardín y terraza", publicacion.getDescripcion()),
            () -> assertEquals("Respetar los horarios de descanso", publicacion.getReglas()),
            () -> assertEquals(new BigDecimal("350000.00"), publicacion.getPrecioPorNoche()),
            () -> assertEquals(new BigDecimal("70000.00"), publicacion.getTarifaLimpieza()),
            () -> assertEquals("APROBACION", publicacion.getModoAceptacion()),
            () -> assertEquals("INACTIVA", publicacion.getEstado()),
            () -> assertEquals(nuevaFechaCreacion, publicacion.getFechaCreacion()),
            () -> assertEquals(nuevaFechaActualizacion, publicacion.getFechaActualizacion())
        );
    }

    @Test
    void debePermitirValoresNulosEnLosAtributos() {
        publicacion.setIdPublicacion(null);
        publicacion.setIdAlojamiento(null);
        publicacion.setIdPolitica(null);
        publicacion.setTitulo(null);
        publicacion.setDescripcion(null);
        publicacion.setReglas(null);
        publicacion.setPrecioPorNoche(null);
        publicacion.setTarifaLimpieza(null);
        publicacion.setModoAceptacion(null);
        publicacion.setEstado(null);
        publicacion.setFechaCreacion(null);
        publicacion.setFechaActualizacion(null);

        assertAll(
            () -> assertNull(publicacion.getIdPublicacion()),
            () -> assertNull(publicacion.getIdAlojamiento()),
            () -> assertNull(publicacion.getIdPolitica()),
            () -> assertNull(publicacion.getTitulo()),
            () -> assertNull(publicacion.getDescripcion()),
            () -> assertNull(publicacion.getReglas()),
            () -> assertNull(publicacion.getPrecioPorNoche()),
            () -> assertNull(publicacion.getTarifaLimpieza()),
            () -> assertNull(publicacion.getModoAceptacion()),
            () -> assertNull(publicacion.getEstado()),
            () -> assertNull(publicacion.getFechaCreacion()),
            () -> assertNull(publicacion.getFechaActualizacion())
        );
    }

    @Test
    void debeAceptarPreciosConDecimales() {
        BigDecimal precio = new BigDecimal("125000.75");
        BigDecimal limpieza = new BigDecimal("25000.50");

        publicacion.setPrecioPorNoche(precio);
        publicacion.setTarifaLimpieza(limpieza);

        assertEquals(precio, publicacion.getPrecioPorNoche());
        assertEquals(limpieza, publicacion.getTarifaLimpieza());
    }

    @Test
    void debeActualizarFechaSinPerderElDesfaseHorario() {
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-20T15:30:00+02:00");

        publicacion.setFechaActualizacion(fecha);

        assertEquals(fecha, publicacion.getFechaActualizacion());
        assertEquals("+02:00", publicacion.getFechaActualizacion().getOffset().toString());
    }
}
