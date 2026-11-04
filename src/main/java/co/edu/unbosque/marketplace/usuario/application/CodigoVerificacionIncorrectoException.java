
package co.edu.unbosque.marketplace.usuario.application;

public class CodigoVerificacionIncorrectoException
        extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    public CodigoVerificacionIncorrectoException(String mensaje) {
        super(mensaje);
    }
}
