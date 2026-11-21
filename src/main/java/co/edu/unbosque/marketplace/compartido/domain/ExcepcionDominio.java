package co.edu.unbosque.marketplace.compartido.domain;

public abstract sealed class ExcepcionDominio extends RuntimeException
        permits DatosInvalidosException,
                SesionRequeridaException,
                AccesoNoPermitidoException,
                RecursoNoEncontradoException,
                ConflictoNegocioException,
                PlazoVencidoException,
                ServicioNoDisponibleException,
                FallaInternaException {

    protected ExcepcionDominio(String mensaje) {
        super(mensaje);
    }

    protected ExcepcionDominio(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
