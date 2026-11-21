package co.edu.unbosque.marketplace.usuario.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class ProteccionDocumentoCorreoTest {
    @Test
    void cifraYDescifraContenidoDeOutbox() {
        String claveCifrado = Base64.getEncoder().encodeToString(new byte[32]);
        String claveHmac = Base64.getEncoder().encodeToString(new byte[32]);
        ProteccionDocumento proteccion = new ProteccionDocumento(claveCifrado, claveHmac);
        var cifrado = proteccion.cifrarTexto("123456");
        assertNotEquals("123456", new String(cifrado.contenido()));
        assertEquals("123456", proteccion.descifrarTexto(cifrado.contenido(), cifrado.nonce()));
    }
}
