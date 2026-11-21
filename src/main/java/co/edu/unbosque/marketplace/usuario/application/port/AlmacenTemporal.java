package co.edu.unbosque.marketplace.usuario.application.port;

import java.time.Duration;

/**
 * Almacén clave-valor de datos temporales con vigencia: códigos de verificación y de recuperación,
 * contadores de intentos y esperas entre reenvíos. La implementación (Redis) vive en infraestructura.
 */
public interface AlmacenTemporal {

    /** Guarda el valor y lo deja vigente durante el tiempo indicado, reemplazando el anterior. */
    void guardar(String clave, String valor, Duration vigencia);

    /** Devuelve el valor, o {@code null} si no existe o ya expiró. */
    String obtener(String clave);

    /** Guarda el valor solo si la clave no existe. Devuelve {@code true} únicamente si lo guardó. */
    boolean guardarSiAusente(String clave, String valor, Duration vigencia);

    void eliminar(String clave);

    /** Suma uno al contador (lo crea en 1 si no existe). Devuelve el nuevo valor, o {@code null} si no se pudo leer. */
    Long incrementar(String clave);

    /** Resta uno al contador. Devuelve el nuevo valor, o {@code null} si no se pudo leer. */
    Long decrementar(String clave);

    /** Cambia la vigencia restante de la clave. */
    void asignarVigencia(String clave, Duration vigencia);

    /** Segundos que le quedan de vigencia a la clave, o {@code null} si no existe. */
    Long segundosRestantes(String clave);
}
