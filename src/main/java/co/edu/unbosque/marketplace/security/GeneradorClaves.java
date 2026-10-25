
package co.edu.unbosque.marketplace.security;

import java.security.SecureRandom;
import java.util.Base64;

public class GeneradorClaves {

    public static void main(String[] args) {
        SecureRandom random = new SecureRandom();

        byte[] claveAES = new byte[32];
        byte[] claveHMAC = new byte[32];

        random.nextBytes(claveAES);
        random.nextBytes(claveHMAC);

        System.out.println("DOCUMENTO_AES_KEY="
                + Base64.getEncoder().encodeToString(claveAES));

        System.out.println("DOCUMENTO_HMAC_KEY="
                + Base64.getEncoder().encodeToString(claveHMAC));
    }
}
