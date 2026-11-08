package co.edu.unbosque.marketplace.usuario.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CodigoVerificacionIncorrectoExceptionTest {

    @Test
    void conservaMensajeYEsUnaIllegalArgumentException() {
        String mensaje = "El código de verificación es incorrecto.";

        CodigoVerificacionIncorrectoException exception =
                new CodigoVerificacionIncorrectoException(mensaje);

        assertAll(
            () -> assertEquals(mensaje, exception.getMessage()),
            () -> assertTrue(exception instanceof IllegalArgumentException)
        );
    }

    @Test
    void permiteMensajeNulo() {
        CodigoVerificacionIncorrectoException exception =
                new CodigoVerificacionIncorrectoException(null);

        assertNull(exception.getMessage());
    }
}
