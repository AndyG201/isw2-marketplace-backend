package co.edu.unbosque.marketplace.auditoria.contracts;

public interface AuditoriaAdministracionCommand {

    void registrarEvento(RegistrarEventoAdministracion solicitud);
}