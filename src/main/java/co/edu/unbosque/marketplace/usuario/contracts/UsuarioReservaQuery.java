package co.edu.unbosque.marketplace.usuario.contracts;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioReservaQuery {

    Optional<UsuarioReservaInfo> consultarDatosHuesped(
            UUID idHuesped
    );
}
