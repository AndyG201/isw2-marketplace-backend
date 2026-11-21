package co.edu.unbosque.marketplace.compartido.domain;

public final class ServicioNoDisponibleException extends ExcepcionDominio {

    public ServicioNoDisponibleException(String mensaje) {
        super(mensaje);
    }

    public ServicioNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
