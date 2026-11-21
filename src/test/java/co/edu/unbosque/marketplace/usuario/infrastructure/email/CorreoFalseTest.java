package co.edu.unbosque.marketplace.usuario.infrastructure.email;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CorreoFalsoTest {

    private CorreoFalso correoFalso;

    @BeforeEach
    void configurar() {
        correoFalso = new CorreoFalso();
    }

    @Test
    void registraCorreoDeVerificacion() {

        correoFalso.enviarCodigoVerificacion(
                "usuario@ejemplo.com",
                "123456"
        );

        List<CorreoFalso.CorreoSimulado> correos =
                correoFalso.obtenerCorreosEnviados();

        assertEquals(1, correos.size());
        assertEquals("usuario@ejemplo.com", correos.get(0).destinatario());
        assertEquals("VERIFICACION", correos.get(0).tipo());
        assertEquals("123456", correos.get(0).codigo());
    }

    @Test
    void registraCorreoDeRecuperacion() {

        correoFalso.enviarCodigoRecuperacion(
                "usuario@ejemplo.com",
                "654321"
        );

        List<CorreoFalso.CorreoSimulado> correos =
                correoFalso.obtenerCorreosEnviados();

        assertEquals(1, correos.size());
        assertEquals("RECUPERACION", correos.get(0).tipo());
        assertEquals("654321", correos.get(0).codigo());
    }

    @Test
    void permiteRegistrarVariosCorreos() {

        correoFalso.enviarCodigoVerificacion(
                "usuario1@ejemplo.com", "111111"
        );

        correoFalso.enviarCodigoRecuperacion(
                "usuario2@ejemplo.com", "222222"
        );

        assertEquals(2, correoFalso.obtenerCorreosEnviados().size());
    }

    @Test
    void permiteLimpiarLosCorreosRegistrados() {

        correoFalso.enviarCodigoVerificacion(
                "usuario@ejemplo.com", "123456"
        );

        correoFalso.limpiar();

        assertTrue(correoFalso.obtenerCorreosEnviados().isEmpty());
    }

    @Test
    void devuelveUnaListaInmodificable() {

        correoFalso.enviarCodigoVerificacion(
                "usuario@ejemplo.com", "123456"
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> correoFalso.obtenerCorreosEnviados().clear()
        );
    }
}
