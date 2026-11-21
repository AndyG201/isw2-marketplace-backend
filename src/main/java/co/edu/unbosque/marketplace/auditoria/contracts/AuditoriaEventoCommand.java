package co.edu.unbosque.marketplace.auditoria.contracts;

public interface AuditoriaEventoCommand {

    /** Inserta el evento en la misma transacción del caso de uso. */
    void registrarEvento(RegistrarEventoAuditoria evento);

    /** Inserta eventos de seguridad o errores sin depender de una transacción llamadora. */
    void registrarEventoIndependiente(RegistrarEventoAuditoria evento);
}