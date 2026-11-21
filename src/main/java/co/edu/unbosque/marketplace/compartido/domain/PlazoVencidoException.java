package co.edu.unbosque.marketplace.compartido.domain;

public final class PlazoVencidoException extends ExcepcionDominio {

    public PlazoVencidoException(String mensaje) {
        super(mensaje);
    }

    public PlazoVencidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
