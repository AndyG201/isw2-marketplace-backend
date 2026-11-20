package co.edu.unbosque.marketplace.alojamiento.contracts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class CrearAlojamientoParaPublicacionTest {

    @Test
    void debeCrearRegistroYConsultarSusAtributos() {
        UUID idAnfitrion = UUID.randomUUID();

        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion,
                        "Bogotá",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        assertEquals(idAnfitrion, solicitud.idAnfitrion());
        assertEquals("Bogotá", solicitud.ubicacion());
        assertEquals("Bogotá", solicitud.ciudad());
        assertEquals("Apartamento", solicitud.tipoAlojamiento());
        assertEquals(4, solicitud.capacidad());
    }

    @Test
    void debeCompararDosRegistrosConLosMismosDatos() {
        UUID idAnfitrion = UUID.randomUUID();

        CrearAlojamientoParaPublicacion solicitud1 =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion, "Bogotá", "Bogotá", "Apartamento", 4
                );

        CrearAlojamientoParaPublicacion solicitud2 =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion, "Bogotá", "Bogotá", "Apartamento", 4
                );

        assertEquals(solicitud1, solicitud2);
        assertEquals(solicitud1.hashCode(), solicitud2.hashCode());
    }

    @Test
    void debeDetectarRegistrosConDatosDiferentes() {
        CrearAlojamientoParaPublicacion solicitud1 =
                new CrearAlojamientoParaPublicacion(
                        UUID.randomUUID(), "Bogotá", "Bogotá", "Apartamento", 4
                );

        CrearAlojamientoParaPublicacion solicitud2 =
                new CrearAlojamientoParaPublicacion(
                        UUID.randomUUID(), "Medellín", "Medellín", "Casa", 6
                );

        assertNotEquals(solicitud1, solicitud2);
    }

    @Test
    void debePermitirValoresNulosEnAtributosDeReferencia() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        null, null, null, null, 0
                );

        assertNull(solicitud.idAnfitrion());
        assertNull(solicitud.ubicacion());
        assertNull(solicitud.ciudad());
        assertNull(solicitud.tipoAlojamiento());
        assertEquals(0, solicitud.capacidad());
    }

    @Test
    void debeConservarUnaCapacidadNegativaSinValidarla() {
        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        UUID.randomUUID(),
                        "Bogotá",
                        "Bogotá",
                        "Apartamento",
                        -2
                );

        assertEquals(-2, solicitud.capacidad());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        UUID idAnfitrion = UUID.randomUUID();

        CrearAlojamientoParaPublicacion solicitud =
                new CrearAlojamientoParaPublicacion(
                        idAnfitrion,
                        "Bogotá",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        String resultado = solicitud.toString();

        assertTrue(resultado.contains(idAnfitrion.toString()));
        assertTrue(resultado.contains("Bogotá"));
        assertTrue(resultado.contains("Apartamento"));
        assertTrue(resultado.contains("4"));
    }
}
