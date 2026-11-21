package co.edu.unbosque.marketplace.compartido.domain;

public final class AccesoNoPermitidoException extends ExcepcionDominio {

    public AccesoNoPermitidoException(String mensaje) {
        super(mensaje);
    }

    public AccesoNoPermitidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
