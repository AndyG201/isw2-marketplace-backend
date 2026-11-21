package co.edu.unbosque.marketplace.compartido.domain;

public final class ConflictoNegocioException extends ExcepcionDominio {

    public ConflictoNegocioException(String mensaje) {
        super(mensaje);
    }

    public ConflictoNegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
