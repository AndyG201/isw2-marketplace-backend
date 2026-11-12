package co.edu.unbosque.marketplace.usuario.api;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import co.edu.unbosque.marketplace.usuario.api.request.RestringirUsuarioRequest;
import co.edu.unbosque.marketplace.usuario.api.response.AdministradorUsuarioResponse;
import co.edu.unbosque.marketplace.usuario.application.AdministradorService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministradorUsuarioControllerTest {

    @Mock
    private AdministradorService administradorService;

    @InjectMocks
    private AdministradorUsuarioController controller;

    private final UUID idUsuario = UUID.randomUUID();

    private RestringirUsuarioRequest solicitudValida() {
        RestringirUsuarioRequest solicitud = new RestringirUsuarioRequest();
        solicitud.setMotivo("Incumplimiento de las normas.");
        return solicitud;
    }

    @Test
    void reactivarBloqueoRetornaOkYEliminaEspaciosDelCorreo() {
        var response = controller.reactivarBloqueo(
            "  usuario@ejemplo.com  "
        );

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "La cuenta ha sido reactivada correctamente.",
                response.getBody()
            )
        );

        verify(administradorService).reactivarBloqueo(
            "usuario@ejemplo.com"
        );
    }

    @Test
    void reactivarBloqueoRetorna404AnteIllegalArgumentException() {
        String correo = "usuario@ejemplo.com";
        String mensaje = "Usuario no encontrado.";

        doThrow(new IllegalArgumentException(mensaje))
            .when(administradorService).reactivarBloqueo(correo);

        var response = controller.reactivarBloqueo(correo);

        assertAll(
            () -> assertEquals(
                HttpStatus.NOT_FOUND, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(administradorService).reactivarBloqueo(correo);
    }

    @Test
    void reactivarBloqueoRetorna409AnteIllegalStateException() {
        String correo = "usuario@ejemplo.com";
        String mensaje = "La cuenta no está bloqueada.";

        doThrow(new IllegalStateException(mensaje))
            .when(administradorService).reactivarBloqueo(correo);

        var response = controller.reactivarBloqueo(correo);

        assertAll(
            () -> assertEquals(
                HttpStatus.CONFLICT, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(administradorService).reactivarBloqueo(correo);
    }

    @Test
    void restringirUsuarioRetornaOk() {
        RestringirUsuarioRequest solicitud = solicitudValida();

        var response = controller.restringirUsuario(idUsuario, solicitud);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "La cuenta ha sido restringida correctamente.",
                response.getBody()
            )
        );

        verify(administradorService).restringirUsuario(
            idUsuario, solicitud
        );
    }

    @Test
    void restringirUsuarioRetorna400AnteIllegalArgumentException() {
        RestringirUsuarioRequest solicitud = solicitudValida();
        String mensaje = "Usuario no encontrado.";

        doThrow(new IllegalArgumentException(mensaje))
            .when(administradorService)
            .restringirUsuario(idUsuario, solicitud);

        var response = controller.restringirUsuario(idUsuario, solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.BAD_REQUEST, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(administradorService).restringirUsuario(
            idUsuario, solicitud
        );
    }

    @Test
    void restringirUsuarioRetorna409AnteIllegalStateException() {
        RestringirUsuarioRequest solicitud = solicitudValida();
        String mensaje = "La cuenta ya está restringida.";

        doThrow(new IllegalStateException(mensaje))
            .when(administradorService)
            .restringirUsuario(idUsuario, solicitud);

        var response = controller.restringirUsuario(idUsuario, solicitud);

        assertAll(
            () -> assertEquals(
                HttpStatus.CONFLICT, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(administradorService).restringirUsuario(
            idUsuario, solicitud
        );
    }

    @Test
    void levantarRestriccionRetornaOk() {
        var response = controller.levantarRestriccion(idUsuario);

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "La restricción administrativa ha sido levantada correctamente.",
                response.getBody()
            )
        );

        verify(administradorService).levantarRestriccion(idUsuario);
    }

    @Test
    void levantarRestriccionRetorna400AnteIllegalArgumentException() {
        String mensaje = "Usuario no encontrado.";

        doThrow(new IllegalArgumentException(mensaje))
            .when(administradorService).levantarRestriccion(idUsuario);

        var response = controller.levantarRestriccion(idUsuario);

        assertAll(
            () -> assertEquals(
                HttpStatus.BAD_REQUEST, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(administradorService).levantarRestriccion(idUsuario);
    }

    @Test
    void levantarRestriccionRetorna409AnteIllegalStateException() {
        String mensaje = "La cuenta no está restringida.";

        doThrow(new IllegalStateException(mensaje))
            .when(administradorService).levantarRestriccion(idUsuario);

        var response = controller.levantarRestriccion(idUsuario);

        assertAll(
            () -> assertEquals(
                HttpStatus.CONFLICT, response.getStatusCode()
            ),
            () -> assertEquals(mensaje, response.getBody())
        );

        verify(administradorService).levantarRestriccion(idUsuario);
    }

    @Test
    void consultarUsuariosRetornaOkConLaListaDelServicio() {
        AdministradorUsuarioResponse usuario =
                new AdministradorUsuarioResponse(
                    idUsuario,
                    "Ana",
                    "Perez",
                    "ana@ejemplo.com",
                    "HUESPED",
                    "ACTIVO",
                    false,
                    null
                );

        List<AdministradorUsuarioResponse> usuarios = List.of(usuario);

        when(administradorService.consultarUsuarios())
            .thenReturn(usuarios);

        var response = controller.consultarUsuarios();

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(usuarios, response.getBody())
        );

        verify(administradorService).consultarUsuarios();
    }

    @Test
    void consultarUsuariosRetornaOkConListaVacia() {
        when(administradorService.consultarUsuarios())
            .thenReturn(Collections.emptyList());

        var response = controller.consultarUsuarios();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(administradorService).consultarUsuarios();
    }
}
