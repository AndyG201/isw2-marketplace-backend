package co.edu.unbosque.marketplace.usuario.infrastructure.email;

import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalHTTPRequestMappingTest {

    private ExternalHTTPRequestMapping servicio;
    private JavaMailSender mailSender;
    private MimeMessage mensaje;

    private static final String CORREO = "usuario@ejemplo.com";
    private static final String CODIGO = "012345";
    private static final String REMITENTE = "marketplace@gmail.com";

    @BeforeEach
    void configurar() throws Exception {

        mailSender = mock(JavaMailSender.class);

        mensaje = new MimeMessage(
                Session.getInstance(new Properties())
        );

        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        servicio = new ExternalHTTPRequestMapping(mailSender);

        ReflectionTestUtils.setField(
                servicio,
                "remitente",
                REMITENTE
        );
    }

    @Test
    void enviarCodigoVerificacionConstruyeYEnviaCorreo() throws Exception {

        servicio.enviarCodigoVerificacion(CORREO, CODIGO);

        ArgumentCaptor<MimeMessage> captor =
                ArgumentCaptor.forClass(MimeMessage.class);

        verify(mailSender).send(captor.capture());

        MimeMessage enviado = captor.getValue();

        String contenidoEsperado =
                "<h2>Verifica tu correo electronico</h2>"
                + "<p>Tu codigo de verificacion es:</p>"
                + "<h1>" + CODIGO + "</h1>"
                + "<p>Si no solicitaste este registro, "
                + "puedes ignorar este mensaje.</p>";

        assertAll(
            () -> assertEquals(
                    REMITENTE,
                    ((InternetAddress) enviado.getFrom()[0]).getAddress()
            ),
            () -> assertEquals(
                    CORREO,
                    ((InternetAddress) enviado.getRecipients(
                            Message.RecipientType.TO
                    )[0]).getAddress()
            ),
            () -> assertEquals(
                    "Verificacion de correo - Marketplace",
                    enviado.getSubject()
            ),
            () -> assertEquals(
                    contenidoEsperado,
                    enviado.getContent()
            ),
            () -> assertTrue(enviado.isMimeType("text/html"))
        );
    }

    @Test
    void enviarCodigoVerificacionEnvuelveExcepcionDeGmail() {

        MailSendException fallo =
                new MailSendException("Fallo en el servidor SMTP");

        doThrow(fallo)
                .when(mailSender)
                .send(any(MimeMessage.class));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> servicio.enviarCodigoVerificacion(CORREO, CODIGO)
        );

        assertAll(
            () -> assertEquals(
                    "No se pudo enviar el correo de verificacion",
                    error.getMessage()
            ),
            () -> assertSame(
                    fallo,
                    error.getCause()
            )
        );

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void enviarCodigoRecuperacionConstruyeYEnviaCorreo() throws Exception {

        servicio.enviarCodigoRecuperacion(CORREO, CODIGO);

        ArgumentCaptor<MimeMessage> captor =
                ArgumentCaptor.forClass(MimeMessage.class);

        verify(mailSender).send(captor.capture());

        MimeMessage enviado = captor.getValue();

        String contenidoEsperado =
                "<h2>Recuperación de contraseña</h2>"
                + "<p>Recibimos una solicitud para restablecer tu contraseña.</p>"
                + "<p>Tu código de recuperación es:</p>"
                + "<h1>" + CODIGO + "</h1>"
                + "<p>Este código vence en 5 minutos.</p>"
                + "<p>Si no solicitaste recuperar tu contraseña, "
                + "ignora este mensaje.</p>";

        assertAll(
            () -> assertEquals(
                    REMITENTE,
                    ((InternetAddress) enviado.getFrom()[0]).getAddress()
            ),
            () -> assertEquals(
                    CORREO,
                    ((InternetAddress) enviado.getRecipients(
                            Message.RecipientType.TO
                    )[0]).getAddress()
            ),
            () -> assertEquals(
                    "Recuperación de contraseña - Marketplace",
                    enviado.getSubject()
            ),
            () -> assertEquals(
                    contenidoEsperado,
                    enviado.getContent()
            ),
            () -> assertTrue(enviado.isMimeType("text/html"))
        );
    }

    @Test
    void enviarCodigoRecuperacionEnvuelveExcepcionDeGmail() {

        MailSendException fallo =
                new MailSendException("Fallo en el servidor SMTP");

        doThrow(fallo)
                .when(mailSender)
                .send(any(MimeMessage.class));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> servicio.enviarCodigoRecuperacion(CORREO, CODIGO)
        );

        assertAll(
            () -> assertEquals(
                    "No se pudo enviar el correo de recuperación",
                    error.getMessage()
            ),
            () -> assertSame(
                    fallo,
                    error.getCause()
            )
        );

        verify(mailSender).send(any(MimeMessage.class));
    }
}
