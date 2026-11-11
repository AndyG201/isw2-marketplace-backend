package co.edu.unbosque.marketplace.usuario.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VerificacionCorreoTest {

    @Test
    void constructorInicializaValoresPorDefecto() {
        VerificacionCorreo verificacion = new VerificacionCorreo();

        assertAll(
            () -> assertNull(verificacion.getIdVerificacion()),
            () -> assertNull(verificacion.getUsuario()),
            () -> assertNull(verificacion.getCodigoHash()),
            () -> assertNull(verificacion.getFechaExpiracion()),
            () -> assertFalse(verificacion.isVerificado()),
            () -> assertEquals(0, verificacion.getIntentos())
        );
    }

    @Test
    void permiteAsignarYActualizarIdentificador() {
        VerificacionCorreo verificacion = new VerificacionCorreo();
        UUID idInicial = UUID.randomUUID();
        UUID idNuevo = UUID.randomUUID();

        verificacion.setIdVerificacion(idInicial);
        assertEquals(idInicial, verificacion.getIdVerificacion());

        verificacion.setIdVerificacion(idNuevo);
        assertEquals(idNuevo, verificacion.getIdVerificacion());
    }

    @Test
    void permiteAsignarYActualizarUsuario() {
        VerificacionCorreo verificacion = new VerificacionCorreo();
        Usuario usuarioInicial = new Usuario();
        Usuario usuarioNuevo = new Usuario();

        verificacion.setUsuario(usuarioInicial);
        assertSame(usuarioInicial, verificacion.getUsuario());

        verificacion.setUsuario(usuarioNuevo);
        assertSame(usuarioNuevo, verificacion.getUsuario());
    }

    @Test
    void permiteAsignarYActualizarCodigoHash() {
        VerificacionCorreo verificacion = new VerificacionCorreo();

        verificacion.setCodigoHash("hash-inicial");
        assertEquals("hash-inicial", verificacion.getCodigoHash());

        verificacion.setCodigoHash("hash-nuevo");
        assertEquals("hash-nuevo", verificacion.getCodigoHash());
    }

    @Test
    void permiteAsignarYActualizarFechaExpiracion() {
        VerificacionCorreo verificacion = new VerificacionCorreo();
        OffsetDateTime fechaInicial =
                OffsetDateTime.parse("2026-01-01T10:00:00-05:00");
        OffsetDateTime fechaNueva = fechaInicial.plusMinutes(10);

        verificacion.setFechaExpiracion(fechaInicial);
        assertEquals(fechaInicial, verificacion.getFechaExpiracion());

        verificacion.setFechaExpiracion(fechaNueva);
        assertEquals(fechaNueva, verificacion.getFechaExpiracion());
    }

    @Test
    void permiteMarcarYDesmarcarVerificacion() {
        VerificacionCorreo verificacion = new VerificacionCorreo();

        verificacion.setVerificado(true);
        assertTrue(verificacion.isVerificado());

        verificacion.setVerificado(false);
        assertFalse(verificacion.isVerificado());
    }

    @Test
    void permiteActualizarYReiniciarIntentos() {
        VerificacionCorreo verificacion = new VerificacionCorreo();

        verificacion.setIntentos(1);
        assertEquals(1, verificacion.getIntentos());

        verificacion.setIntentos(5);
        assertEquals(5, verificacion.getIntentos());

        verificacion.setIntentos(0);
        assertEquals(0, verificacion.getIntentos());
    }

    @Test
    void permiteLimpiarCamposDeReferencia() {
        VerificacionCorreo verificacion = new VerificacionCorreo();
        verificacion.setIdVerificacion(UUID.randomUUID());
        verificacion.setUsuario(new Usuario());
        verificacion.setCodigoHash("hash-prueba");
        verificacion.setFechaExpiracion(
            OffsetDateTime.parse("2026-01-01T10:00:00-05:00")
        );

        verificacion.setIdVerificacion(null);
        verificacion.setUsuario(null);
        verificacion.setCodigoHash(null);
        verificacion.setFechaExpiracion(null);

        assertAll(
            () -> assertNull(verificacion.getIdVerificacion()),
            () -> assertNull(verificacion.getUsuario()),
            () -> assertNull(verificacion.getCodigoHash()),
            () -> assertNull(verificacion.getFechaExpiracion())
        );
    }
}