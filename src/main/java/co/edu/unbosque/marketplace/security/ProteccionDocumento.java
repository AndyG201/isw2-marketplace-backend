
package co.edu.unbosque.marketplace.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProteccionDocumento {

    private final SecretKey claveCifrado;
    private final SecretKeySpec claveHmac;

    private final SecureRandom random = new SecureRandom();

    public ProteccionDocumento(
            @Value("${seguridad.documento.clave-cifrado}") String claveCifradoBase64,
            @Value("${seguridad.documento.clave-hmac}") String claveHmacBase64) {

        byte[] bytesCifrado = Base64.getDecoder()
                .decode(claveCifradoBase64);

        byte[] bytesHmac = Base64.getDecoder()
                .decode(claveHmacBase64);

        if (bytesCifrado.length != 32) {
            throw new IllegalArgumentException(
                    "La clave AES debe tener 32 bytes.");
        }

        if (bytesHmac.length < 32) {
            throw new IllegalArgumentException(
                    "La clave HMAC debe tener al menos 32 bytes.");
        }

        this.claveCifrado = new SecretKeySpec(bytesCifrado, "AES");
        this.claveHmac = new SecretKeySpec(bytesHmac, "HmacSHA256");
    }

    public DatosDocumento protegerDocumento(
            String tipoDocumento, String documento) {

        try {
            String documentoNormalizado =
                    documento.trim()
                            .replaceAll("[^A-Za-z0-9]", "")
                            .toUpperCase();

            byte[] nonce = new byte[12];
            random.nextBytes(nonce);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    claveCifrado,
                    new GCMParameterSpec(128, nonce)
            );

            byte[] documentoCifrado = cipher.doFinal(
                    documentoNormalizado.getBytes(StandardCharsets.UTF_8)
            );

            byte[] documentoHash = calcularHashDocumento(
                    tipoDocumento,
                    documentoNormalizado
            );

            return new DatosDocumento(
                    documentoCifrado,
                    nonce,
                    documentoHash
            );

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No fue posible proteger el documento.", e);
        }
    }

    public byte[] calcularHashDocumento(
            String tipoDocumento, String documento) {

        try {
            String documentoNormalizado =
                    documento.trim()
                            .replaceAll("[^A-Za-z0-9]", "")
                            .toUpperCase();

            String dato = tipoDocumento.trim().toUpperCase()
                    + ":" + documentoNormalizado;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(claveHmac);

            return mac.doFinal(
                    dato.getBytes(StandardCharsets.UTF_8)
            );

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No fue posible calcular el hash del documento.", e);
        }
    }

    public String descifrarDocumento(
            byte[] documentoCifrado, byte[] nonce) {

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    claveCifrado,
                    new GCMParameterSpec(128, nonce)
            );

            byte[] documentoOriginal =
                    cipher.doFinal(documentoCifrado);

            return new String(
                    documentoOriginal,
                    StandardCharsets.UTF_8
            );

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No fue posible descifrar el documento.", e);
        }
    }

    public record DatosDocumento(
            byte[] documentoCifrado,
            byte[] documentoNonce,
            byte[] documentoHash) {
    }
}
