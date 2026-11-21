package co.edu.unbosque.marketplace.compartido.domain;

public final class SesionRequeridaException extends ExcepcionDominio {

    public SesionRequeridaException(String mensaje) {
        super(mensaje);
    }

    public SesionRequeridaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
