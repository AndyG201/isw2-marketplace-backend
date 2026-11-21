package co.edu.unbosque.marketplace.compartido.domain;

public final class DatosInvalidosException extends ExcepcionDominio {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }

    public DatosInvalidosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
