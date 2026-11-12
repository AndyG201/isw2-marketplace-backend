package co.edu.unbosque.marketplace.usuario.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProteccionDocumentoTest {

    private static final String CLAVE_CIFRADO =
            Base64.getEncoder().encodeToString(
                    "12345678901234567890123456789012"
                            .getBytes(StandardCharsets.UTF_8)
            );

    // Clave HMAC de 32 bytes como mínimo.
    private static final String CLAVE_HMAC =
            Base64.getEncoder().encodeToString(
                    "HmacClavePrueba12345678901234567"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private ProteccionDocumento proteccionDocumento;

    @BeforeEach
    void setUp() {
        proteccionDocumento = new ProteccionDocumento(
                CLAVE_CIFRADO,
                CLAVE_HMAC
        );
    }

    @Test
    void testProtegerDocumento() {

        ProteccionDocumento.DatosDocumento resultado =
                proteccionDocumento.protegerDocumento(
                        "CC",
                        "123456789"
                );

        assertNotNull(resultado);
        assertNotNull(resultado.documentoCifrado());
        assertNotNull(resultado.documentoNonce());
        assertNotNull(resultado.documentoHash());

        assertEquals(12, resultado.documentoNonce().length);
        assertEquals(32, resultado.documentoHash().length);
    }

    @Test
    void testDescifrarDocumento() {

        ProteccionDocumento.DatosDocumento resultado =
                proteccionDocumento.protegerDocumento(
                        "CC",
                        "123456789"
                );

        String documentoDescifrado =
                proteccionDocumento.descifrarDocumento(
                        resultado.documentoCifrado(),
                        resultado.documentoNonce()
                );

        assertEquals("123456789", documentoDescifrado);
    }

    @Test
    void testNormalizarDocumento() {

        ProteccionDocumento.DatosDocumento resultado =
                proteccionDocumento.protegerDocumento(
                        "CC",
                        " ab-123  cd "
                );

        String documentoDescifrado =
                proteccionDocumento.descifrarDocumento(
                        resultado.documentoCifrado(),
                        resultado.documentoNonce()
                );

        assertEquals("AB123CD", documentoDescifrado);
    }

    @Test
    void testCalcularHashDocumento() {

        byte[] hash = proteccionDocumento.calcularHashDocumento(
                "CC",
                "123456789"
        );

        assertNotNull(hash);
        assertEquals(32, hash.length);
    }

    @Test
    void testHashIgualParaDocumentosNormalizados() {

        byte[] hash1 = proteccionDocumento.calcularHashDocumento(
                "CC",
                "123-456-789"
        );

        byte[] hash2 = proteccionDocumento.calcularHashDocumento(
                "cc",
                "123456789"
        );

        assertArrayEquals(hash1, hash2);
    }

    @Test
    void testHashDiferenteParaTiposDeDocumentoDistintos() {

        byte[] hash1 = proteccionDocumento.calcularHashDocumento(
                "CC",
                "123456789"
        );

        byte[] hash2 = proteccionDocumento.calcularHashDocumento(
                "TI",
                "123456789"
        );

        assertFalse(Arrays.equals(hash1, hash2));
    }

    @Test
    void testCifradoGeneraNonceDiferente() {

        ProteccionDocumento.DatosDocumento resultado1 =
                proteccionDocumento.protegerDocumento(
                        "CC",
                        "123456789"
                );

        ProteccionDocumento.DatosDocumento resultado2 =
                proteccionDocumento.protegerDocumento(
                        "CC",
                        "123456789"
                );

        assertFalse(Arrays.equals(
                resultado1.documentoNonce(),
                resultado2.documentoNonce()
        ));

        assertFalse(Arrays.equals(
                resultado1.documentoCifrado(),
                resultado2.documentoCifrado()
        ));
    }

    @Test
    void testDescifrarDocumentoConNonceIncorrecto() {

        ProteccionDocumento.DatosDocumento resultado =
                proteccionDocumento.protegerDocumento(
                        "CC",
                        "123456789"
                );

        byte[] nonceIncorrecto =
                resultado.documentoNonce().clone();

        nonceIncorrecto[0] ^= 1;

        assertThrows(
                IllegalStateException.class,
                () -> proteccionDocumento.descifrarDocumento(
                        resultado.documentoCifrado(),
                        nonceIncorrecto
                )
        );
    }

    @Test
    void testDescifrarDocumentoCifradoAlterado() {

        ProteccionDocumento.DatosDocumento resultado =
                proteccionDocumento.protegerDocumento(
                        "CC",
                        "123456789"
                );

        byte[] cifradoAlterado =
                resultado.documentoCifrado().clone();

        cifradoAlterado[0] ^= 1;

        assertThrows(
                IllegalStateException.class,
                () -> proteccionDocumento.descifrarDocumento(
                        cifradoAlterado,
                        resultado.documentoNonce()
                )
        );
    }

    @Test
    void testClaveCifradoInvalida() {

        String claveInvalida = Base64.getEncoder()
                .encodeToString(
                        "claveCorta".getBytes(StandardCharsets.UTF_8)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProteccionDocumento(
                        claveInvalida,
                        CLAVE_HMAC
                )
        );
    }

    @Test
    void testClaveHmacInvalida() {

        String claveInvalida = Base64.getEncoder()
                .encodeToString(
                        "claveCorta".getBytes(StandardCharsets.UTF_8)
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProteccionDocumento(
                        CLAVE_CIFRADO,
                        claveInvalida
                )
        );
    }
}
