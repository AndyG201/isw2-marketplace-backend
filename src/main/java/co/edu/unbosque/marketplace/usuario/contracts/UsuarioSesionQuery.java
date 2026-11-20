package co.edu.unbosque.marketplace.usuario.contracts;

import java.util.Optional;

public interface UsuarioSesionQuery {

    Optional<UsuarioSesionInfo> consultarPorCorreo(String correo);
}