
package co.edu.unbosque.marketplace.usuario.infrastructure.email;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.usuario.domain.ServicioCorreo;

/**
 * Adaptador simulado para pruebas.
 *
 * Registra los correos solicitados en memoria sin establecer
 * conexiones SMTP ni enviar mensajes reales.
 */
@Component
@Profile("test")
public class CorreoFalso implements ServicioCorreo {

    private final List<CorreoSimulado> correos =
            new CopyOnWriteArrayList<>();

    @Override
    public void enviarCodigoVerificacion(
            String correoDestino,
            String codigo) {

        correos.add(new CorreoSimulado(
                correoDestino,
                "VERIFICACION",
                codigo
        ));
    }

    @Override
    public void enviarCodigoRecuperacion(
            String correoDestino,
            String codigo) {

        correos.add(new CorreoSimulado(
                correoDestino,
                "RECUPERACION",
                codigo
        ));
    }

    /**
     * Devuelve los mensajes registrados durante las pruebas.
     */
    public List<CorreoSimulado> obtenerCorreosEnviados() {
        return List.copyOf(correos);
    }

    /**
     * Limpia los mensajes registrados.
     */
    public void limpiar() {
        correos.clear();
    }

    /**
     * Representación de un correo simulado.
     */
    public record CorreoSimulado(
            String destinatario,
            String tipo,
            String codigo) {
    }
}
