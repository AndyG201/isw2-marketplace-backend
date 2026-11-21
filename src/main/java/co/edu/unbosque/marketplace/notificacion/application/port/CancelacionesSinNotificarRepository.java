package co.edu.unbosque.marketplace.notificacion.application.port;

import java.util.List;

public interface CancelacionesSinNotificarRepository {

    List<CancelacionPendienteNotificacion> bloquearPendientes(int limite);
}
