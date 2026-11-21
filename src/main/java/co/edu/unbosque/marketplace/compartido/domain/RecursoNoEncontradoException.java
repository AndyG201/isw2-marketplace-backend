package co.edu.unbosque.marketplace.compartido.domain;

public final class RecursoNoEncontradoException extends ExcepcionDominio {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public RecursoNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
