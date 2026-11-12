package co.edu.unbosque.marketplace.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class PasswordConfigTest {

    private PasswordConfig passwordConfig;

    @BeforeEach
    void setUp() {
        passwordConfig = new PasswordConfig();
    }

    @Test
    void testPasswordEncoderNoNulo() {

        PasswordEncoder encoder = passwordConfig.passwordEncoder();

        assertNotNull(encoder);
    }

    @Test
    void testTipoPasswordEncoder() {

        PasswordEncoder encoder = passwordConfig.passwordEncoder();

        assertInstanceOf(BCryptPasswordEncoder.class, encoder);
    }

    @Test
    void testCodificarContrasena() {

        PasswordEncoder encoder = passwordConfig.passwordEncoder();

        String contrasena = "ClaveSegura123";
        String hash = encoder.encode(contrasena);

        assertNotNull(hash);
        assertNotEquals(contrasena, hash);
    }

    @Test
    void testVerificarContrasenaCorrecta() {

        PasswordEncoder encoder = passwordConfig.passwordEncoder();

        String contrasena = "ClaveSegura123";
        String hash = encoder.encode(contrasena);

        assertTrue(encoder.matches(contrasena, hash));
    }

    @Test
    void testVerificarContrasenaIncorrecta() {

        PasswordEncoder encoder = passwordConfig.passwordEncoder();

        String hash = encoder.encode("ClaveSegura123");

        assertFalse(encoder.matches("OtraClave456", hash));
    }

    @Test
    void testHashesDiferentesParaMismaContrasena() {

        PasswordEncoder encoder = passwordConfig.passwordEncoder();

        String contrasena = "ClaveSegura123";

        String hash1 = encoder.encode(contrasena);
        String hash2 = encoder.encode(contrasena);

        assertNotEquals(hash1, hash2);
        assertTrue(encoder.matches(contrasena, hash1));
        assertTrue(encoder.matches(contrasena, hash2));
    }
}
