package co.edu.unbosque.marketplace.alojamiento.contracts;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionInfo;

class AlojamientoPublicacionInfoTest {

    @Test
    void debeCrearRecordConTodosLosDatos() {
        UUID idAlojamiento = UUID.randomUUID();
        UUID idAnfitrion = UUID.randomUUID();

        AlojamientoPublicacionInfo informacion =
                new AlojamientoPublicacionInfo(
                        idAlojamiento,
                        idAnfitrion,
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        assertEquals(idAlojamiento, informacion.idAlojamiento());
        assertEquals(idAnfitrion, informacion.idAnfitrion());
        assertEquals("Calle 123", informacion.ubicacion());
        assertEquals("Bogotá", informacion.ciudad());
        assertEquals("Apartamento", informacion.tipoAlojamiento());
        assertEquals(4, informacion.capacidad());
    }

    @Test
    void debeCompararRecordsConLosMismosDatos() {
        UUID idAlojamiento = UUID.randomUUID();
        UUID idAnfitrion = UUID.randomUUID();

        AlojamientoPublicacionInfo informacion1 =
                new AlojamientoPublicacionInfo(
                        idAlojamiento, idAnfitrion,
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        AlojamientoPublicacionInfo informacion2 =
                new AlojamientoPublicacionInfo(
                        idAlojamiento, idAnfitrion,
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        assertEquals(informacion1, informacion2);
        assertEquals(informacion1.hashCode(), informacion2.hashCode());
    }

    @Test
    void debeDetectarRecordsConDatosDiferentes() {
        AlojamientoPublicacionInfo informacion1 =
                new AlojamientoPublicacionInfo(
                        UUID.randomUUID(), UUID.randomUUID(),
                        "Calle 123", "Bogotá", "Apartamento", 4
                );

        AlojamientoPublicacionInfo informacion2 =
                new AlojamientoPublicacionInfo(
                        UUID.randomUUID(), UUID.randomUUID(),
                        "Carrera 15", "Medellín", "Casa", 6
                );

        assertNotEquals(informacion1, informacion2);
    }

    @Test
    void debePermitirValoresNulosEnAtributosDeReferencia() {
        AlojamientoPublicacionInfo informacion =
                new AlojamientoPublicacionInfo(
                        null, null, null, null, null, 4
                );

        assertNull(informacion.idAlojamiento());
        assertNull(informacion.idAnfitrion());
        assertNull(informacion.ubicacion());
        assertNull(informacion.ciudad());
        assertNull(informacion.tipoAlojamiento());
        assertEquals(4, informacion.capacidad());
    }

    @Test
    void debePermitirCapacidadCero() {
        AlojamientoPublicacionInfo informacion =
                new AlojamientoPublicacionInfo(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        0
                );

        assertEquals(0, informacion.capacidad());
    }

    @Test
    void debeGenerarRepresentacionEnTexto() {
        AlojamientoPublicacionInfo informacion =
                new AlojamientoPublicacionInfo(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Calle 123",
                        "Bogotá",
                        "Apartamento",
                        4
                );

        assertTrue(informacion.toString().contains("Calle 123"));
        assertTrue(informacion.toString().contains("Bogotá"));
        assertTrue(informacion.toString().contains("Apartamento"));
        assertTrue(informacion.toString().contains("capacidad=4"));
    }
}
