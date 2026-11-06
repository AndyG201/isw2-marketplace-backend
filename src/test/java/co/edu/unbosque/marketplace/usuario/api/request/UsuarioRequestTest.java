package co.edu.unbosque.marketplace.usuario.api.request;

import java.time.OffsetDateTime;
import java.util.UUID;

import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioRequestTest {

    @Test
    void constructorInicializaValoresPorDefecto() {
        UsuarioRequest request = new UsuarioRequest();

        assertAll(
            () -> assertNull(request.getIdUsuario()),
            () -> assertNull(request.getIdRol()),
            () -> assertNull(request.getNombre()),
            () -> assertNull(request.getApellido()),
            () -> assertNull(request.getCorreo()),
            () -> assertNull(request.getTipoDocumento()),
            () -> assertNull(request.getDocumentoCifrado()),
            () -> assertNull(request.getDocumentoNonce()),
            () -> assertNull(request.getDocumentoHash()),
            () -> assertNull(request.getTelefono()),
            () -> assertNull(request.getContrasenaHash()),
            () -> assertFalse(request.isCorreoVerificado()),
            () -> assertNull(request.getEstado()),
            () -> assertFalse(request.isRestringido()),
            () -> assertEquals(0, request.getIntentosFallidos()),
            () -> assertNull(request.getMotivoRestriccion()),
            () -> assertNull(request.getFechaRegistro()),
            () -> assertNull(request.getFechaActualizacion())
        );
    }

    @Test
    void gettersDevuelvenValoresAsignados() {
        UsuarioRequest request = new UsuarioRequest();

        UUID idUsuario = UUID.randomUUID();
        UUID idRol = UUID.randomUUID();

        byte[] documentoCifrado = {10, 20, 30};
        byte[] documentoNonce = {40, 50, 60};
        byte[] documentoHash = {70, 80, 90};

        OffsetDateTime fechaRegistro =
                OffsetDateTime.parse("2026-01-10T10:00:00-05:00");
        OffsetDateTime fechaActualizacion =
                OffsetDateTime.parse("2026-01-11T12:30:00-05:00");

        request.setIdUsuario(idUsuario);
        request.setIdRol(idRol);
        request.setNombre("Ana");
        request.setApellido("Perez");
        request.setCorreo("ana@ejemplo.com");
        request.setTipoDocumento("CEDULA");
        request.setDocumentoCifrado(documentoCifrado);
        request.setDocumentoNonce(documentoNonce);
        request.setDocumentoHash(documentoHash);
        request.setTelefono("3001234567");
        request.setContrasenaHash("hashContrasena");
        request.setCorreoVerificado(true);
        request.setRestringido(true);
        request.setIntentosFallidos(3);
        request.setMotivoRestriccion("Incumplimiento de las normas.");
        request.setFechaRegistro(fechaRegistro);
        request.setFechaActualizacion(fechaActualizacion);

        assertAll(
            () -> assertEquals(idUsuario, request.getIdUsuario()),
            () -> assertEquals(idRol, request.getIdRol()),
            () -> assertEquals("Ana", request.getNombre()),
            () -> assertEquals("Perez", request.getApellido()),
            () -> assertEquals("ana@ejemplo.com", request.getCorreo()),
            () -> assertEquals("CEDULA", request.getTipoDocumento()),
            () -> assertArrayEquals(
                documentoCifrado, request.getDocumentoCifrado()
            ),
            () -> assertArrayEquals(
                documentoNonce, request.getDocumentoNonce()
            ),
            () -> assertArrayEquals(
                documentoHash, request.getDocumentoHash()
            ),
            () -> assertEquals("3001234567", request.getTelefono()),
            () -> assertEquals(
                "hashContrasena", request.getContrasenaHash()
            ),
            () -> assertTrue(request.isCorreoVerificado()),
            () -> assertTrue(request.isRestringido()),
            () -> assertEquals(3, request.getIntentosFallidos()),
            () -> assertEquals(
                "Incumplimiento de las normas.",
                request.getMotivoRestriccion()
            ),
            () -> assertEquals(
                fechaRegistro, request.getFechaRegistro()
            ),
            () -> assertEquals(
                fechaActualizacion, request.getFechaActualizacion()
            )
        );
    }

    @Test
    void permiteAsignarLosEstadosDelEnum() {
        UsuarioRequest request = new UsuarioRequest();

        for (EstadoUsuario estado : EstadoUsuario.values()) {
            request.setEstado(estado);

            assertEquals(estado, request.getEstado());
        }

        request.setEstado(null);
        assertNull(request.getEstado());
    }

    @Test
    void permiteCambiarBooleanosDeTrueAFalse() {
        UsuarioRequest request = new UsuarioRequest();
        request.setCorreoVerificado(true);
        request.setRestringido(true);

        request.setCorreoVerificado(false);
        request.setRestringido(false);

        assertAll(
            () -> assertFalse(request.isCorreoVerificado()),
            () -> assertFalse(request.isRestringido())
        );
    }

    @Test
    void permiteReiniciarIntentosFallidos() {
        UsuarioRequest request = new UsuarioRequest();
        request.setIntentosFallidos(3);

        request.setIntentosFallidos(0);

        assertEquals(0, request.getIntentosFallidos());
    }
}
