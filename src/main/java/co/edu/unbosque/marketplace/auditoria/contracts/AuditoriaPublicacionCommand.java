
package co.edu.unbosque.marketplace.auditoria.contracts;

public interface AuditoriaPublicacionCommand {

    void registrarEvento(
            RegistrarEventoPublicacion solicitud
    );
}
