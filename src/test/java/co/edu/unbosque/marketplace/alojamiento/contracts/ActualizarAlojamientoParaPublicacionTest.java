package co.edu.unbosque.marketplace.alojamiento.contracts;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


class ActualizarAlojamientoParaPublicacionTest {

    @Test
    void debeCrearRecordConTodosLosDatos() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        assertEquals("Calle 123", solicitud.ubicacion());
        assertEquals("Bogotá", solicitud.ciudad());
        assertEquals("Apartamento", solicitud.tipoAlojamiento());
        assertEquals(4, solicitud.capacidad());
    }

    @Test
    void debeCompararDosRecordsConLosMismosDatos() {
        ActualizarAlojamientoParaPublicacion solicitud1 =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        ActualizarAlojamientoParaPublicacion solicitud2 =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        assertEquals(solicitud1, solicitud2);
        assertEquals(solicitud1.hashCode(), solicitud2.hashCode());
    }

    @Test
    void debeDetectarRecordsConDatosDiferentes() {
        ActualizarAlojamientoParaPublicacion solicitud1 =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        ActualizarAlojamientoParaPublicacion solicitud2 =
                new ActualizarAlojamientoParaPublicacion(
                        "Carrera 15", "Medellín", "Casa", 6
                );

        assertNotEquals(solicitud1, solicitud2);
    }

    @Test
    void debePermitirValoresNulosEnAtributosDeTexto() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        null, null, null, 4
                );

        assertNull(solicitud.ubicacion());
        assertNull(solicitud.ciudad());
        assertNull(solicitud.tipoAlojamiento());
        assertEquals(4, solicitud.capacidad());
    }

    @Test
    void debePermitirCapacidadCero() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", 0
                );

        assertEquals(0, solicitud.capacidad());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        assertTrue(solicitud.toString().contains("Calle 123"));
        assertTrue(solicitud.toString().contains("Bogotá"));
        assertTrue(solicitud.toString().contains("Apartamento"));
    }
}
