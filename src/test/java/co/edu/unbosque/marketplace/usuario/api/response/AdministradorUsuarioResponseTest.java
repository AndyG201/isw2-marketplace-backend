package co.edu.unbosque.marketplace.usuario.api.response;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorUsuarioResponseTest {

    @Test
    void constructorAsignaValoresYGettersLosDevuelven() {
        UUID idUsuario = UUID.randomUUID();

        AdministradorUsuarioResponse response =
                new AdministradorUsuarioResponse(
                    idUsuario,
                    "Ana",
                    "Perez",
                    "ana@ejemplo.com",
                    "HUESPED",
                    "ACTIVO",
                    true,
                    "Incumplimiento de las normas."
                );

        assertAll(
            () -> assertEquals(idUsuario, response.getIdUsuario()),
            () -> assertEquals("Ana", response.getNombre()),
            () -> assertEquals("Perez", response.getApellido()),
            () -> assertEquals("ana@ejemplo.com", response.getCorreo()),
            () -> assertEquals("HUESPED", response.getRol()),
            () -> assertEquals("ACTIVO", response.getEstado()),
            () -> assertTrue(response.isRestringido()),
            () -> assertEquals(
                "Incumplimiento de las normas.",
                response.getMotivoRestriccion()
            )
        );
    }

    @Test
    void permiteUsuarioSinRestriccion() {
        AdministradorUsuarioResponse response =
                new AdministradorUsuarioResponse(
                    UUID.randomUUID(),
                    "Carlos",
                    "Lopez",
                    "carlos@ejemplo.com",
                    "ANFITRION",
                    "ACTIVO",
                    false,
                    null
                );

        assertAll(
            () -> assertFalse(response.isRestringido()),
            () -> assertNull(response.getMotivoRestriccion())
        );
    }

    @Test
    void constructorPermiteCamposDeReferenciaNulos() {
        AdministradorUsuarioResponse response =
                new AdministradorUsuarioResponse(
                    null, null, null, null,
                    null, null, false, null
                );

        assertAll(
            () -> assertNull(response.getIdUsuario()),
            () -> assertNull(response.getNombre()),
            () -> assertNull(response.getApellido()),
            () -> assertNull(response.getCorreo()),
            () -> assertNull(response.getRol()),
            () -> assertNull(response.getEstado()),
            () -> assertFalse(response.isRestringido()),
            () -> assertNull(response.getMotivoRestriccion())
        );
    }
}
