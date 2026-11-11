package co.edu.unbosque.marketplace.usuario.contracts;


public record UsuarioPublicacionInfo(
        boolean esAnfitrion,
        boolean cuentaHabilitadaParaPublicar,
        boolean cuentaActiva,
        boolean restringido,
        boolean correoVerificado,
        int intentosFallidos
) {
}
