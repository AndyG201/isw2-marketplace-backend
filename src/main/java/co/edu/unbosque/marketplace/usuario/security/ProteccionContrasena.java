
package co.edu.unbosque.marketplace.usuario.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ProteccionContrasena {

    private final BCryptPasswordEncoder encoder =
            new BCryptPasswordEncoder(12);

    public String generarHash(String contrasena) {
        return encoder.encode(contrasena);
    }

    public boolean verificar(
            String contrasenaIngresada,
            String contrasenaHashGuardada) {

        return encoder.matches(
                contrasenaIngresada,
                contrasenaHashGuardada
        );
    }
}
