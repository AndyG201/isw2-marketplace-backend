
package co.edu.unbosque.marketplace.usuario.domain;

/**
 * Puerto de salida para el envío de correos electrónicos.
 *
 * Define las operaciones de comunicación por correo
 * utilizadas por el módulo de usuarios.
 *
 * Las implementaciones concretas pertenecen a infraestructura.
 */
public interface ServicioCorreo {

    /**
     * Envía un código para verificar una dirección de correo.
     *
     * @param correoDestino dirección del destinatario.
     * @param codigo código de verificación.
     */
    void enviarCodigoVerificacion(String correoDestino, String codigo);

    /**
     * Envía un código para recuperar una contraseña.
     *
     * @param correoDestino dirección del destinatario.
     * @param codigo código de recuperación.
     */
    void enviarCodigoRecuperacion(String correoDestino, String codigo);
}
