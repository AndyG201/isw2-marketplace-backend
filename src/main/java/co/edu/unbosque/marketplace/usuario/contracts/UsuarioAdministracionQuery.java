
package co.edu.unbosque.marketplace.usuario.contracts;

import java.util.UUID;

public interface UsuarioAdministracionQuery {

    boolean esAdministrador(UUID idUsuario);
}
