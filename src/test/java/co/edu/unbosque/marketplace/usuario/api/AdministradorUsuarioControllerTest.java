package co.edu.unbosque.marketplace.usuario.api;

import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import co.edu.unbosque.marketplace.usuario.application.AdministradorService;
import co.edu.unbosque.marketplace.usuario.application.dto.AdministradorUsuarioResponse;
import co.edu.unbosque.marketplace.usuario.application.dto.ModificarDocumentoRequest;
import co.edu.unbosque.marketplace.usuario.application.dto.RestringirUsuarioRequest;
import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.DatosInvalidosException;
import co.edu.unbosque.marketplace.compartido.domain.RecursoNoEncontradoException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static co.edu.unbosque.marketplace.config.TraductorDeErroresPrueba.traducir;
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
        var response = traducir(() -> controller.reactivarBloqueo(
            "  usuario@ejemplo.com  "
        ));

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
    void reactivarBloqueoRetorna404AnteRecursoNoEncontradoException() {
        String correo = "usuario@ejemplo.com";
        String mensaje = "Usuario no encontrado.";

        doThrow(new RecursoNoEncontradoException(mensaje))
            .when(administradorService).reactivarBloqueo(correo);

        var response = traducir(() -> controller.reactivarBloqueo(correo));

        assertAll(
            () -> assertEquals(
                HttpStatus.NOT_FOUND, response.getStatusCode()
            ),
            () -> assertEquals(Map.of("mensaje", mensaje), response.getBody())
        );

        verify(administradorService).reactivarBloqueo(correo);
    }

    @Test
    void reactivarBloqueoRetorna409AnteConflictoNegocioException() {
        String correo = "usuario@ejemplo.com";
        String mensaje = "La cuenta no está bloqueada.";

        doThrow(new ConflictoNegocioException(mensaje))
            .when(administradorService).reactivarBloqueo(correo);

        var response = traducir(() -> controller.reactivarBloqueo(correo));

        assertAll(
            () -> assertEquals(
                HttpStatus.CONFLICT, response.getStatusCode()
            ),
            () -> assertEquals(Map.of("mensaje", mensaje), response.getBody())
        );

        verify(administradorService).reactivarBloqueo(correo);
    }

    @Test
    void restringirUsuarioRetornaOk() {
        RestringirUsuarioRequest solicitud = solicitudValida();

        var response = traducir(() -> controller.restringirUsuario(idUsuario, solicitud));

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
    void restringirUsuarioRetorna400AnteDatosInvalidosException() {
        RestringirUsuarioRequest solicitud = solicitudValida();
        String mensaje = "Usuario no encontrado.";

        doThrow(new DatosInvalidosException(mensaje))
            .when(administradorService)
            .restringirUsuario(idUsuario, solicitud);

        var response = traducir(() -> controller.restringirUsuario(idUsuario, solicitud));

        assertAll(
            () -> assertEquals(
                HttpStatus.BAD_REQUEST, response.getStatusCode()
            ),
            () -> assertEquals(Map.of("mensaje", mensaje), response.getBody())
        );

        verify(administradorService).restringirUsuario(
            idUsuario, solicitud
        );
    }

    @Test
    void restringirUsuarioRetorna409AnteConflictoNegocioException() {
        RestringirUsuarioRequest solicitud = solicitudValida();
        String mensaje = "La cuenta ya está restringida.";

        doThrow(new ConflictoNegocioException(mensaje))
            .when(administradorService)
            .restringirUsuario(idUsuario, solicitud);

        var response = traducir(() -> controller.restringirUsuario(idUsuario, solicitud));

        assertAll(
            () -> assertEquals(
                HttpStatus.CONFLICT, response.getStatusCode()
            ),
            () -> assertEquals(Map.of("mensaje", mensaje), response.getBody())
        );

        verify(administradorService).restringirUsuario(
            idUsuario, solicitud
        );
    }

    @Test
    void levantarRestriccionRetornaOk() {
        var response = traducir(() -> controller.levantarRestriccion(idUsuario));

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
    void levantarRestriccionRetorna400AnteDatosInvalidosException() {
        String mensaje = "Usuario no encontrado.";

        doThrow(new DatosInvalidosException(mensaje))
            .when(administradorService).levantarRestriccion(idUsuario);

        var response = traducir(() -> controller.levantarRestriccion(idUsuario));

        assertAll(
            () -> assertEquals(
                HttpStatus.BAD_REQUEST, response.getStatusCode()
            ),
            () -> assertEquals(Map.of("mensaje", mensaje), response.getBody())
        );

        verify(administradorService).levantarRestriccion(idUsuario);
    }

    @Test
    void levantarRestriccionRetorna409AnteConflictoNegocioException() {
        String mensaje = "La cuenta no está restringida.";

        doThrow(new ConflictoNegocioException(mensaje))
            .when(administradorService).levantarRestriccion(idUsuario);

        var response = traducir(() -> controller.levantarRestriccion(idUsuario));

        assertAll(
            () -> assertEquals(
                HttpStatus.CONFLICT, response.getStatusCode()
            ),
            () -> assertEquals(Map.of("mensaje", mensaje), response.getBody())
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

        var response = traducir(() -> controller.consultarUsuarios());

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

        var response = traducir(() -> controller.consultarUsuarios());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(((java.util.Collection<?>) response.getBody()).isEmpty());

        verify(administradorService).consultarUsuarios();
    }

    @Test
    void modificarDocumentoRetornaOk() {
        ModificarDocumentoRequest solicitud = new ModificarDocumentoRequest("CE", "AB12345");

        var response = traducir(() -> controller.modificarDocumento(idUsuario, solicitud));

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(
                "El documento de la cuenta se actualizó correctamente.",
                response.getBody()
            )
        );
        verify(administradorService).modificarDocumento(idUsuario, solicitud);
    }

    @Test
    void modificarDocumentoRetorna400Y409SegunElError() {
        ModificarDocumentoRequest solicitud = new ModificarDocumentoRequest("CC", "12345");

        doThrow(new DatosInvalidosException("Usuario no encontrado."))
            .when(administradorService).modificarDocumento(idUsuario, solicitud);
        var noEncontrado = traducir(() -> controller.modificarDocumento(idUsuario, solicitud));

        doThrow(new ConflictoNegocioException("Ya existe otra cuenta con ese documento."))
            .when(administradorService).modificarDocumento(idUsuario, solicitud);
        var repetido = traducir(() -> controller.modificarDocumento(idUsuario, solicitud));

        assertAll(
            () -> assertEquals(HttpStatus.BAD_REQUEST, noEncontrado.getStatusCode()),
            () -> assertEquals(Map.of("mensaje", "Usuario no encontrado."), noEncontrado.getBody()),
            () -> assertEquals(HttpStatus.CONFLICT, repetido.getStatusCode()),
            () -> assertEquals(Map.of("mensaje", "Ya existe otra cuenta con ese documento."), repetido.getBody())
        );
    }
}
