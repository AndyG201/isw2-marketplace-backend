
package co.edu.unbosque.marketplace.usuario.infrastructure.email;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import co.edu.unbosque.marketplace.usuario.domain.ServicioCorreo;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Adaptador real para el envío de correos electrónicos.
 *
 * Implementa el puerto ServicioCorreo utilizando
 * Spring Mail y el proveedor SMTP configurado.
 */
@Component
@Profile("!test")
public class CorreoProveedor implements ServicioCorreo {

    private final JavaMailSender mailSender;
    private final String remitente;

    public CorreoProveedor(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remitente) {

        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarCodigoVerificacion(
            String correoDestino,
            String codigo) {

        String asunto = "Verificacion de correo - Marketplace";

        String contenido =
                "<h2>Verifica tu correo electronico</h2>"
                + "<p>Tu codigo de verificacion es:</p>"
                + "<h1>" + codigo + "</h1>"
                + "<p>Si no solicitaste este registro, "
                + "puedes ignorar este mensaje.</p>";

        enviarCorreo(
                correoDestino,
                asunto,
                contenido,
                "No se pudo enviar el correo de verificacion"
        );
    }

    @Override
    public void enviarCodigoRecuperacion(
            String correoDestino,
            String codigo) {

        String asunto = "Recuperación de contraseña - Marketplace";

        String contenido =
                "<h2>Recuperación de contraseña</h2>"
                + "<p>Recibimos una solicitud para restablecer tu contraseña.</p>"
                + "<p>Tu código de recuperación es:</p>"
                + "<h1>" + codigo + "</h1>"
                + "<p>Este código vence en 5 minutos.</p>"
                + "<p>Si no solicitaste recuperar tu contraseña, "
                + "ignora este mensaje.</p>";

        enviarCorreo(
                correoDestino,
                asunto,
                contenido,
                "No se pudo enviar el correo de recuperación"
        );
    }

    /**
     * Construye y envía un correo electrónico HTML.
     */
    private void enviarCorreo(
            String correoDestino,
            String asunto,
            String contenido,
            String mensajeError) {

        try {

            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(remitente);
            helper.setTo(correoDestino);
            helper.setSubject(asunto);
            helper.setText(contenido, true);

            mensaje.saveChanges();

            mailSender.send(mensaje);

        } catch (MessagingException | MailException error) {

            throw new RuntimeException(mensajeError, error);
        }
    }
}
