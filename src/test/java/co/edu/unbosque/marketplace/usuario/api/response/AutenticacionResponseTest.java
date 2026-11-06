package co.edu.unbosque.marketplace.usuario.api.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutenticacionResponseTest {

    @Test
    void constructorVacioInicializaTokenNuloYTipoBearer() {
        AutenticacionResponse response = new AutenticacionResponse();

        assertAll(
            () -> assertNull(response.getToken()),
            () -> assertEquals("Bearer", response.getTipo())
        );
    }

    @Test
    void constructorConTokenAsignaTokenYConservaTipoBearer() {
        AutenticacionResponse response =
                new AutenticacionResponse("token-prueba");

        assertAll(
            () -> assertEquals("token-prueba", response.getToken()),
            () -> assertEquals("Bearer", response.getTipo())
        );
    }

    @Test
    void setterPermiteActualizarToken() {
        AutenticacionResponse response =
                new AutenticacionResponse("token-inicial");

        response.setToken("token-nuevo");

        assertEquals("token-nuevo", response.getToken());
    }

    @Test
    void setterPermiteActualizarTipo() {
        AutenticacionResponse response = new AutenticacionResponse();

        response.setTipo("Basic");

        assertEquals("Basic", response.getTipo());
    }

    @Test
    void constructorPermiteTokenNulo() {
        AutenticacionResponse response = new AutenticacionResponse(null);

        assertAll(
            () -> assertNull(response.getToken()),
            () -> assertEquals("Bearer", response.getTipo())
        );
    }

    @Test
    void settersPermitenValoresNulos() {
        AutenticacionResponse response =
                new AutenticacionResponse("token-prueba");

        response.setToken(null);
        response.setTipo(null);

        assertAll(
            () -> assertNull(response.getToken()),
            () -> assertNull(response.getTipo())
        );
    }
}
