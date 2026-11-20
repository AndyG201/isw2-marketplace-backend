package co.edu.unbosque.marketplace.notificacion.contracts;

public interface NotificacionUsuarioCommand {

    void crearNotificacion(CrearNotificacionUsuario solicitud);
}