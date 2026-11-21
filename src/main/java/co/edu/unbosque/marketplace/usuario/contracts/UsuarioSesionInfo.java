package co.edu.unbosque.marketplace.usuario.contracts;

import java.util.UUID;

public record UsuarioSesionInfo(
        UUID idUsuario,
        boolean administrador) {
}