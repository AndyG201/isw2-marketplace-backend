package co.edu.unbosque.marketplace.usuario.api.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsultarCuentaResponseTest {

    @Test
    void constructorAsignaValoresYGettersLosDevuelven() {
        ConsultarCuentaResponse response = new ConsultarCuentaResponse(
            "Ana",
            "Perez",
            "ana@ejemplo.com",
            "CEDULA",
            "123456789",
            "HUESPED"
        );

        assertAll(
            () -> assertEquals("Ana", response.getNombre()),
            () -> assertEquals("Perez", response.getApellido()),
            () -> assertEquals("ana@ejemplo.com", response.getCorreo()),
            () -> assertEquals("CEDULA", response.getTipoDocumento()),
            () -> assertEquals(
                "123456789", response.getDocumentoIdentidad()
            ),
            () -> assertEquals("HUESPED", response.getRol())
        );
    }

    @Test
    void constructorPermiteValoresNulos() {
        ConsultarCuentaResponse response = new ConsultarCuentaResponse(
            null, null, null, null, null, null
        );

        assertAll(
            () -> assertNull(response.getNombre()),
            () -> assertNull(response.getApellido()),
            () -> assertNull(response.getCorreo()),
            () -> assertNull(response.getTipoDocumento()),
            () -> assertNull(response.getDocumentoIdentidad()),
            () -> assertNull(response.getRol())
        );
    }

    @Test
    void constructorConservaCerosInicialesDelDocumento() {
        ConsultarCuentaResponse response = new ConsultarCuentaResponse(
            "Carlos",
            "Lopez",
            "carlos@ejemplo.com",
            "CEDULA",
            "001234567",
            "ANFITRION"
        );

        assertEquals("001234567", response.getDocumentoIdentidad());
    }
}