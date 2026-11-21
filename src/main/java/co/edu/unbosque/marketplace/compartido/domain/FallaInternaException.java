package co.edu.unbosque.marketplace.compartido.domain;

public final class FallaInternaException extends ExcepcionDominio {

    public FallaInternaException(String mensaje) {
        super(mensaje);
    }

    public FallaInternaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
