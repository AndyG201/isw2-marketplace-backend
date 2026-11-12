package co.edu.unbosque.marketplace.usuario.security;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProteccionContrasenaTest {

    private ProteccionContrasena proteccionContrasena;

    @BeforeEach
    void setUp() {
        proteccionContrasena = new ProteccionContrasena();
    }

    @Test
    void testGenerarHash() {
        String contrasena = "Prueba123*";

        String hash = proteccionContrasena.generarHash(contrasena);

        assertNotNull(hash);
        assertNotEquals(contrasena, hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    }

    @Test
    void testGenerarHashDiferenteCadaVez() {
        String contrasena = "Prueba123*";

        String hash1 = proteccionContrasena.generarHash(contrasena);
        String hash2 = proteccionContrasena.generarHash(contrasena);

        assertNotEquals(hash1, hash2);
    }

    @Test
    void testVerificarContrasenaCorrecta() {
        String contrasena = "Prueba123*";

        String hash = proteccionContrasena.generarHash(contrasena);

        assertTrue(
                proteccionContrasena.verificar(contrasena, hash)
        );
    }

    @Test
    void testVerificarContrasenaIncorrecta() {
        String contrasena = "Prueba123*";

        String hash = proteccionContrasena.generarHash(contrasena);

        assertFalse(
                proteccionContrasena.verificar("OtraClave456*", hash)
        );
    }

    @Test
    void testVerificarHashInvalido() {
        assertFalse(
                proteccionContrasena.verificar(
                        "Prueba123*",
                        "hashInvalido"
                )
        );
    }
}
