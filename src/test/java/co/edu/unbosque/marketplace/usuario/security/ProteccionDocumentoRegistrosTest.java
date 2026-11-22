package co.edu.unbosque.marketplace.usuario.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento.DatosDocumento;
import co.edu.unbosque.marketplace.usuario.security.ProteccionDocumento.TextoCifrado;

/** Los registros con bytes se comparan por su contenido y no muestran los bytes cifrados en su texto. */
class ProteccionDocumentoRegistrosTest {

    @Test
    void textoCifradoConLosMismosBytesEsIgual() {
        TextoCifrado uno = new TextoCifrado(new byte[] {1, 2, 3}, new byte[] {9});
        TextoCifrado otro = new TextoCifrado(new byte[] {1, 2, 3}, new byte[] {9});

        assertEquals(uno, otro);
        assertEquals(uno.hashCode(), otro.hashCode());
    }

    @Test
    void textoCifradoConOtrosBytesEsDistinto() {
        TextoCifrado base = new TextoCifrado(new byte[] {1, 2, 3}, new byte[] {9});

        assertNotEquals(base, new TextoCifrado(new byte[] {1, 2, 4}, new byte[] {9}));
        assertNotEquals(base, new TextoCifrado(new byte[] {1, 2, 3}, new byte[] {8}));
        assertNotEquals(base, null);

        // Un objeto de otro tipo, visto como Object: equals debe devolver false sin fallar.
        Object deOtroTipo = "texto";
        assertNotEquals(base, deOtroTipo);
    }

    @Test
    void datosDocumentoConLosMismosBytesEsIgual() {
        DatosDocumento uno = new DatosDocumento(new byte[] {1}, new byte[] {2}, new byte[] {3});
        DatosDocumento otro = new DatosDocumento(new byte[] {1}, new byte[] {2}, new byte[] {3});

        assertEquals(uno, otro);
        assertEquals(uno.hashCode(), otro.hashCode());
    }

    @Test
    void datosDocumentoConOtrosBytesEsDistinto() {
        DatosDocumento base = new DatosDocumento(new byte[] {1}, new byte[] {2}, new byte[] {3});

        assertNotEquals(base, new DatosDocumento(new byte[] {9}, new byte[] {2}, new byte[] {3}));
        assertNotEquals(base, new DatosDocumento(new byte[] {1}, new byte[] {9}, new byte[] {3}));
        assertNotEquals(base, new DatosDocumento(new byte[] {1}, new byte[] {2}, new byte[] {9}));
        assertNotEquals(base, null);
    }

    @Test
    void elTextoMuestraSoloLasLongitudesNuncaLosBytes() {
        String cifrado = new TextoCifrado(new byte[] {101, 102, 103}, new byte[] {7}).toString();
        String documento = new DatosDocumento(new byte[] {55, 56}, null, new byte[0]).toString();

        assertEquals("TextoCifrado[contenido=3 bytes, nonce=1 bytes]", cifrado);
        assertEquals("DatosDocumento[documentoCifrado=2 bytes, documentoNonce=null, documentoHash=0 bytes]",
                documento);
        assertFalse(cifrado.contains("101"));
        assertTrue(documento.contains("null"));
    }
}
