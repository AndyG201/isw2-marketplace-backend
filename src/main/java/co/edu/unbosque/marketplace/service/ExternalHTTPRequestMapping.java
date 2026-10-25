
package co.edu.unbosque.marketplace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

@Component
public class ExternalHTTPRequestMapping {

    @Value("${resend.api-key}")
    private String apiKey;

    public void enviarCodigoVerificacion(String correoDestino, String codigo) {

        Resend resend = new Resend(apiKey);

        CreateEmailOptions parametros = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(correoDestino)
                .subject("Verificacion de correo - Marketplace")
                .html(
                    "<h2>Verifica tu correo electronico</h2>"
                    + "<p>Tu codigo de verificacion es:</p>"
                    + "<h1>" + codigo + "</h1>"
                    + "<p>Si no solicitaste este registro, "
                    + "puedes ignorar este mensaje.</p>"
                )
                .build();

        try {
            CreateEmailResponse respuesta =
                    resend.emails().send(parametros);

            System.out.println(
                    "Correo enviado. ID: " + respuesta.getId());

        } catch (ResendException e) {
            throw new RuntimeException(
                    "No se pudo enviar el correo de verificacion", e);
        }
    }
}
