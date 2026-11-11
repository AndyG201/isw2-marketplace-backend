package co.edu.unbosque.marketplace.usuario.contracts;

import java.util.UUID;

public record UsuarioReservaInfo(
        UUID idUsuario,
        String nombreCompleto
) {
}
