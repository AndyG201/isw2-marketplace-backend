
package co.edu.unbosque.marketplace.usuario.contracts;

import java.util.List;
import java.util.UUID;

public interface UsuarioAdministracionQuery {

    boolean esAdministrador(UUID idUsuario);

    List<UUID> consultarAdministradoresActivos();
}
