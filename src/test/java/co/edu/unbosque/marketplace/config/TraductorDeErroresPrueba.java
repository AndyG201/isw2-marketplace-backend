package co.edu.unbosque.marketplace.config;

import java.util.function.Supplier;

import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.compartido.domain.ExcepcionDominio;

/**
 * Ayuda para las pruebas que llaman directamente al controlador: los controladores ya no
 * traducen las excepciones de dominio, de eso se encarga {@link ErroresApiHandler}.
 * Esta clase aplica ese mismo traductor para poder comprobar el código HTTP y el mensaje.
 */
public final class TraductorDeErroresPrueba {

    private static final ErroresApiHandler MANEJADOR = new ErroresApiHandler();

    private TraductorDeErroresPrueba() {
    }

    public static ResponseEntity<?> traducir(Supplier<ResponseEntity<?>> accion) {
        try {
            return accion.get();
        } catch (ExcepcionDominio error) {
            return MANEJADOR.manejar(error);
        }
    }
}
