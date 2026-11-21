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
                        4,
                        3,
                        4,
                        2,
                        "Ana Gómez",
                        "Colombia",
                        "CO"
                );

        assertAll(
                () -> assertEquals("Calle 123", solicitud.ubicacion()),
                () -> assertEquals("Bogotá", solicitud.ciudad()),
                () -> assertEquals("Apartamento", solicitud.tipoAlojamiento()),
                () -> assertEquals(4, solicitud.capacidad()),
                () -> assertEquals(3, solicitud.habitaciones()),
                () -> assertEquals(4, solicitud.camas()),
                () -> assertEquals(2, solicitud.banos()),
                () -> assertEquals("Ana Gómez", solicitud.propietarioInmueble()),
                () -> assertEquals("Colombia", solicitud.pais()),
                () -> assertEquals("CO", solicitud.codigoPais())
        );
    }

    @Test
    void debeCompararDosRecordsConLosMismosDatos() {

        ActualizarAlojamientoParaPublicacion solicitud1 =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4,
                        3,
                        4,
                        2,
                        "Ana Gómez",
                        "Colombia",
                        "CO"
                );

        ActualizarAlojamientoParaPublicacion solicitud2 =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4,
                        3,
                        4,
                        2,
                        "Ana Gómez",
                        "Colombia",
                        "CO"
                );

        assertEquals(solicitud1, solicitud2);
        assertEquals(solicitud1.hashCode(), solicitud2.hashCode());
    }

    @Test
    void debeDetectarRecordsConDatosDiferentes() {

        ActualizarAlojamientoParaPublicacion solicitud1 =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4,
                        3,
                        4,
                        2,
                        "Ana Gómez",
                        "Colombia",
                        "CO"
                );

        ActualizarAlojamientoParaPublicacion solicitud2 =
                new ActualizarAlojamientoParaPublicacion(
                        "Carrera 15",
                        "Medellín",
                        "Casa",
                        6,
                        2,
                        3,
                        2,
                        "Carlos Pérez",
                        "Colombia",
                        "CO"
                );

        assertNotEquals(solicitud1, solicitud2);
    }

    @Test
    void debePermitirValoresNulosEnAtributosOpcionales() {

        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        null,
                        null,
                        null,
                        4,
                        null,
                        null,
                        null
                );

        assertAll(
                () -> assertNull(solicitud.ubicacion()),
                () -> assertNull(solicitud.ciudad()),
                () -> assertNull(solicitud.tipoAlojamiento()),
                () -> assertEquals(4, solicitud.capacidad()),
                () -> assertNull(solicitud.habitaciones()),
                () -> assertNull(solicitud.camas()),
                () -> assertNull(solicitud.banos())
        );
    }

    @Test
    void debePermitirCapacidadCero() {

        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        0,
                        null,
                        null,
                        null
                );

        assertEquals(0, solicitud.capacidad());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {

        ActualizarAlojamientoParaPublicacion solicitud =
                new ActualizarAlojamientoParaPublicacion(
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4,
                        3,
                        4,
                        2,
                        "Ana Gómez",
                        "Colombia",
                        "CO"
                );

        assertAll(
                () -> assertTrue(solicitud.toString().contains("Calle 123")),
                () -> assertTrue(solicitud.toString().contains("Bogotá")),
                () -> assertTrue(solicitud.toString().contains("Apartamento")),
                () -> assertTrue(solicitud.toString().contains("Ana Gómez")),
                () -> assertTrue(solicitud.toString().contains("Colombia")),
                () -> assertTrue(solicitud.toString().contains("CO"))
        );
    }
}
