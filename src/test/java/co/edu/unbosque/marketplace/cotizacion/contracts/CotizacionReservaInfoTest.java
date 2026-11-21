package co.edu.unbosque.marketplace.cotizacion.contracts;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CotizacionReservaInfoTest {

    @Test
    void constructorYAccesores() {
        UUID idCotizacion = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        LocalDate fechaEntrada = LocalDate.of(2026, 12, 10);
        LocalDate fechaSalida = LocalDate.of(2026, 12, 15);

        CotizacionReservaInfo info = new CotizacionReservaInfo(
                idCotizacion,
                idPublicacion,
                fechaEntrada,
                fechaSalida
        );

        assertEquals(idCotizacion, info.idCotizacion());
        assertEquals(idPublicacion, info.idPublicacion());
        assertEquals(fechaEntrada, info.fechaEntrada());
        assertEquals(fechaSalida, info.fechaSalida());
    }

    @Test
    void permiteValoresNulos() {
        CotizacionReservaInfo info = new CotizacionReservaInfo(
                null,
                null,
                null,
                null
        );

        assertNull(info.idCotizacion());
        assertNull(info.idPublicacion());
        assertNull(info.fechaEntrada());
        assertNull(info.fechaSalida());
    }

    @Test
    void dosRegistrosConLosMismosDatosSonIguales() {
        UUID idCotizacion = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();
        LocalDate fechaEntrada = LocalDate.of(2026, 12, 10);
        LocalDate fechaSalida = LocalDate.of(2026, 12, 15);

        CotizacionReservaInfo primero = new CotizacionReservaInfo(
                idCotizacion, idPublicacion, fechaEntrada, fechaSalida
        );

        CotizacionReservaInfo segundo = new CotizacionReservaInfo(
                idCotizacion, idPublicacion, fechaEntrada, fechaSalida
        );

        assertEquals(primero, segundo);
        assertEquals(primero.hashCode(), segundo.hashCode());
    }

    @Test
    void dosRegistrosConDatosDiferentesNoSonIguales() {
        CotizacionReservaInfo primero = new CotizacionReservaInfo(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15)
        );

        CotizacionReservaInfo segundo = new CotizacionReservaInfo(
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2026, 12, 11),
                LocalDate.of(2026, 12, 16)
        );

        assertNotEquals(primero, segundo);
    }

    @Test
    void toStringContieneLosDatosDelRegistro() {
        UUID idCotizacion = UUID.randomUUID();
        UUID idPublicacion = UUID.randomUUID();

        CotizacionReservaInfo info = new CotizacionReservaInfo(
                idCotizacion,
                idPublicacion,
                LocalDate.of(2026, 12, 10),
                LocalDate.of(2026, 12, 15)
        );

        assertTrue(info.toString().contains(idCotizacion.toString()));
        assertTrue(info.toString().contains(idPublicacion.toString()));
        assertTrue(info.toString().contains("2026-12-10"));
        assertTrue(info.toString().contains("2026-12-15"));
    }
}
