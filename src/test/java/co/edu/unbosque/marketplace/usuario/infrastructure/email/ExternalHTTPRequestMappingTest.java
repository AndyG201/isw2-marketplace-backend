package co.edu.unbosque.marketplace.usuario.infrastructure.email;

import java.util.List;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalHTTPRequestMappingTest {

    private ExternalHTTPRequestMapping servicio;

    private static final String CORREO = "usuario@ejemplo.com";
    private static final String CODIGO = "012345";
    private static final String API_KEY = "re_clave_de_prueba";

    @BeforeEach
    void configurar() {
        servicio = new ExternalHTTPRequestMapping();

        ReflectionTestUtils.setField(servicio, "apiKey", API_KEY);
    }

    @Test
    void enviarCodigoVerificacionConstruyeYEnviaCorreo() throws Exception {
        CreateEmailResponse respuesta = mock(CreateEmailResponse.class);
        when(respuesta.getId()).thenReturn("correo-verificacion-123");

        try (var construccion = mockConstruction(
                Resend.class,
                withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                (resend, contexto) -> {
                    assertEquals(List.of(API_KEY), contexto.arguments());

                    when(resend.emails().send(any(CreateEmailOptions.class)))
                        .thenReturn(respuesta);
                })) {

            servicio.enviarCodigoVerificacion(CORREO, CODIGO);

            assertEquals(1, construccion.constructed().size());
            Resend resend = construccion.constructed().get(0);

            ArgumentCaptor<CreateEmailOptions> captor =
                    ArgumentCaptor.forClass(CreateEmailOptions.class);

            verify(resend.emails()).send(captor.capture());

            CreateEmailOptions parametros = captor.getValue();

            assertAll(
                () -> assertEquals(
                    "onboarding@resend.dev", parametros.getFrom()
                ),
                () -> assertEquals(
                    List.of(CORREO), parametros.getTo()
                ),
                () -> assertEquals(
                    "Verificacion de correo - Marketplace",
                    parametros.getSubject()
                ),
                () -> assertEquals(
                    "<h2>Verifica tu correo electronico</h2>"
                        + "<p>Tu codigo de verificacion es:</p>"
                        + "<h1>" + CODIGO + "</h1>"
                        + "<p>Si no solicitaste este registro, "
                        + "puedes ignorar este mensaje.</p>",
                    parametros.getHtml()
                )
            );
        }
    }

    @Test
    void enviarCodigoVerificacionEnvuelveExcepcionDeResend() {
        ResendException fallo = mock(ResendException.class);

        try (var construccion = mockConstruction(
                Resend.class,
                withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                (resend, contexto) -> {
                    when(resend.emails().send(any(CreateEmailOptions.class)))
                        .thenThrow(fallo);
                })) {

            RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> servicio.enviarCodigoVerificacion(CORREO, CODIGO)
            );

            assertAll(
                () -> assertEquals(
                    "No se pudo enviar el correo de verificacion",
                    error.getMessage()
                ),
                () -> assertSame(fallo, error.getCause())
            );

            assertEquals(1, construccion.constructed().size());
        }
    }

    @Test
    void enviarCodigoRecuperacionConstruyeYEnviaCorreo() throws Exception {
        CreateEmailResponse respuesta = mock(CreateEmailResponse.class);
        when(respuesta.getId()).thenReturn("correo-recuperacion-123");

        try (var construccion = mockConstruction(
                Resend.class,
                withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                (resend, contexto) -> {
                    assertEquals(List.of(API_KEY), contexto.arguments());

                    when(resend.emails().send(any(CreateEmailOptions.class)))
                        .thenReturn(respuesta);
                })) {

            servicio.enviarCodigoRecuperacion(CORREO, CODIGO);

            assertEquals(1, construccion.constructed().size());
            Resend resend = construccion.constructed().get(0);

            ArgumentCaptor<CreateEmailOptions> captor =
                    ArgumentCaptor.forClass(CreateEmailOptions.class);

            verify(resend.emails()).send(captor.capture());

            CreateEmailOptions parametros = captor.getValue();

            assertAll(
                () -> assertEquals(
                    "onboarding@resend.dev", parametros.getFrom()
                ),
                () -> assertEquals(
                    List.of(CORREO), parametros.getTo()
                ),
                () -> assertEquals(
                    "Recuperación de contraseña - Marketplace",
                    parametros.getSubject()
                ),
                () -> assertEquals(
                    "<h2>Recuperación de contraseña</h2>"
                        + "<p>Recibimos una solicitud para restablecer tu contraseña.</p>"
                        + "<p>Tu código de recuperación es:</p>"
                        + "<h1>" + CODIGO + "</h1>"
                        + "<p>Este código vence en 5 minutos.</p>"
                        + "<p>Si no solicitaste recuperar tu contraseña, "
                        + "ignora este mensaje.</p>",
                    parametros.getHtml()
                )
            );
        }
    }

    @Test
    void enviarCodigoRecuperacionEnvuelveExcepcionDeResend() {
        ResendException fallo = mock(ResendException.class);

        try (var construccion = mockConstruction(
                Resend.class,
                withSettings().defaultAnswer(RETURNS_DEEP_STUBS),
                (resend, contexto) -> {
                    when(resend.emails().send(any(CreateEmailOptions.class)))
                        .thenThrow(fallo);
                })) {

            RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> servicio.enviarCodigoRecuperacion(CORREO, CODIGO)
            );

            assertAll(
                () -> assertEquals(
                    "No se pudo enviar el correo de recuperación",
                    error.getMessage()
                ),
                () -> assertSame(fallo, error.getCause())
            );

            assertEquals(1, construccion.constructed().size());
        }
    }
}
