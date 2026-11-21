package co.edu.unbosque.marketplace.publicacion.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;

class ModoAceptacionReservaTest {

    @Test
    void debeRetornarModoInmediata() {
        ModoAceptacionReserva resultado =
                ModoAceptacionReserva.desde("INMEDIATA");

        assertEquals(ModoAceptacionReserva.INMEDIATA, resultado);
    }

    @Test
    void debeRetornarModoAprobacion() {
        ModoAceptacionReserva resultado =
                ModoAceptacionReserva.desde("APROBACION");

        assertEquals(ModoAceptacionReserva.APROBACION, resultado);
    }

    @Test
    void debeAceptarTextoEnMinusculas() {
        assertEquals(
                ModoAceptacionReserva.INMEDIATA,
                ModoAceptacionReserva.desde("inmediata")
        );

        assertEquals(
                ModoAceptacionReserva.APROBACION,
                ModoAceptacionReserva.desde("aprobacion")
        );
    }

    @Test
    void debeAceptarTextoConEspacios() {
        assertEquals(
                ModoAceptacionReserva.INMEDIATA,
                ModoAceptacionReserva.desde("  INMEDIATA  ")
        );

        assertEquals(
                ModoAceptacionReserva.APROBACION,
                ModoAceptacionReserva.desde("  APROBACION  ")
        );
    }

    @Test
    void debeLanzarExcepcionCuandoElValorEsNulo() {
        DatosInvalidosException exception = assertThrows(
                DatosInvalidosException.class,
                () -> ModoAceptacionReserva.desde(null)
        );

        assertEquals(
                "El modo de aceptación es obligatorio.",
                exception.getMessage()
        );
    }

    @Test
    void debeLanzarExcepcionCuandoElValorEstaVacio() {
        DatosInvalidosException exception = assertThrows(
                DatosInvalidosException.class,
                () -> ModoAceptacionReserva.desde("")
        );

        assertEquals(
                "El modo de aceptación es obligatorio.",
                exception.getMessage()
        );
    }

    @Test
    void debeLanzarExcepcionCuandoElValorContieneSoloEspacios() {
        DatosInvalidosException exception = assertThrows(
                DatosInvalidosException.class,
                () -> ModoAceptacionReserva.desde("   ")
        );

        assertEquals(
                "El modo de aceptación es obligatorio.",
                exception.getMessage()
        );
    }

    @Test
    void debeLanzarExcepcionCuandoElModoEsInvalido() {
        DatosInvalidosException exception = assertThrows(
                DatosInvalidosException.class,
                () -> ModoAceptacionReserva.desde("AUTOMATICA")
        );

        assertEquals(
                "El modo de aceptación debe ser INMEDIATA o APROBACION.",
                exception.getMessage()
        );
    }

    @Test
    void debeLanzarExcepcionCuandoElValorNoExiste() {
        assertThrows(
                DatosInvalidosException.class,
                () -> ModoAceptacionReserva.desde("RESERVA")
        );
    }
}
