package co.edu.unbosque.marketplace.publicacion.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionBusquedaCriteria;

class PublicacionBusquedaServiceValidacionTest {

    private static final LocalDate HOY = LocalDate.of(2030, 1, 10);

    private static PublicacionBusquedaCriteria criterio(
            LocalDate inicio, LocalDate fin, Integer capacidad,
            BigDecimal min, BigDecimal max, int pagina, int tamano) {
        return new PublicacionBusquedaCriteria(
                " Bogotá ", inicio, fin, capacidad, " Casa ", min, max, List.of(), pagina, tamano);
    }

    private static void rechaza(PublicacionBusquedaCriteria criterio) {
        assertThrows(DatosInvalidosException.class, () -> PublicacionBusquedaService.normalizarYValidar(criterio));
    }

    @Test
    void rechazaFechasIncompletasOInvertidas() {
        rechaza(criterio(HOY, null, null, null, null, 0, 20));
        rechaza(criterio(null, HOY, null, null, null, 0, 20));
        rechaza(criterio(HOY, HOY, null, null, null, 0, 20));
    }

    @Test
    void rechazaCapacidadPreciosPaginaYTamanoInvalidos() {
        rechaza(criterio(null, null, 0, null, null, 0, 20));
        rechaza(criterio(null, null, null, new BigDecimal("-1"), null, 0, 20));
        rechaza(criterio(null, null, null, null, new BigDecimal("-1"), 0, 20));
        rechaza(criterio(null, null, null, new BigDecimal("10"), new BigDecimal("5"), 0, 20));
        rechaza(criterio(null, null, null, null, null, -1, 20));
        rechaza(criterio(null, null, null, null, null, 0, 0));
        rechaza(criterio(null, null, null, null, null, 0, 101));
    }

    @Test
    void normalizaCiudadYTipoAMinusculasSinEspacios() {
        PublicacionBusquedaCriteria resultado = PublicacionBusquedaService.normalizarYValidar(
                criterio(HOY, HOY.plusDays(2), 2, null, null, 0, 20));

        assertEquals("bogotá", resultado.ciudad());
        assertEquals("casa", resultado.tipoAlojamiento());
    }

    @Test
    void ciudadEnBlancoQuedaNula() {
        PublicacionBusquedaCriteria resultado = PublicacionBusquedaService.normalizarYValidar(
                new PublicacionBusquedaCriteria("  ", null, null, null, null, null, null,
                        List.of(UUID.randomUUID()), 0, 20));

        assertNull(resultado.ciudad());
    }
}
