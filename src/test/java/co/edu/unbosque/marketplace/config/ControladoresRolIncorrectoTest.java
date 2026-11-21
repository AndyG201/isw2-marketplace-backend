package co.edu.unbosque.marketplace.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import co.edu.unbosque.marketplace.cancelacion.api.ReservaCancelacionController;
import co.edu.unbosque.marketplace.cancelacion.application.CancelacionReservaService;
import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.reporte.api.ExportacionReporteController;
import co.edu.unbosque.marketplace.reporte.application.ExportacionReporteService;
import co.edu.unbosque.marketplace.resena.api.ResenaController;
import co.edu.unbosque.marketplace.resena.api.ResenaModeracionController;
import co.edu.unbosque.marketplace.resena.application.ResenaModeracionService;
import co.edu.unbosque.marketplace.resena.application.ResenaService;
import co.edu.unbosque.marketplace.reserva.api.ReservaAnfitrionController;
import co.edu.unbosque.marketplace.reserva.api.ReservaHistorialController;
import co.edu.unbosque.marketplace.reserva.api.ReservaPagoSimuladoController;
import co.edu.unbosque.marketplace.reserva.application.ReservaAnfitrionService;
import co.edu.unbosque.marketplace.reserva.application.ReservaHistorialService;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoSimuladoService;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioSesionQuery;

/**
 * Los controladores que exigen un rol lanzan AccesoNoPermitidoException (403) cuando la sesión no lo tiene;
 * ErroresApiHandler la traduce a HTTP.
 */
class ControladoresRolIncorrectoTest {

    private final UUID usuario = UUID.randomUUID();
    private final UUID idRecurso = UUID.randomUUID();

    private static Authentication conRol(String rol) {
        return new UsernamePasswordAuthenticationToken("alguien@ejemplo.com", "x",
                List.of(new SimpleGrantedAuthority(rol)));
    }

    @Test
    void cancelarReservaExigeHuesped() {
        CancelacionReservaService servicio = mock(CancelacionReservaService.class);
        ReservaCancelacionController controller = new ReservaCancelacionController(servicio);

        assertThrows(AccesoNoPermitidoException.class, () -> controller.cancelar(usuario, null, idRecurso, null));
        assertThrows(AccesoNoPermitidoException.class,
                () -> controller.cancelar(usuario, conRol("ROLE_ANFITRION"), idRecurso, null));
        verifyNoInteractions(servicio);
    }

    @Test
    void crearResenaExigeHuesped() {
        ResenaService servicio = mock(ResenaService.class);
        ResenaController controller = new ResenaController(servicio);

        assertThrows(AccesoNoPermitidoException.class,
                () -> controller.crear(usuario, conRol("ROLE_ANFITRION"), idRecurso, null));
        verifyNoInteractions(servicio);
    }

    @Test
    void ocultarResenaExigeAdministrador() {
        ResenaModeracionService servicio = mock(ResenaModeracionService.class);
        ResenaModeracionController controller = new ResenaModeracionController(servicio);

        assertThrows(AccesoNoPermitidoException.class,
                () -> controller.ocultar(usuario, conRol("ROLE_HUESPED"), idRecurso, null));
        verifyNoInteractions(servicio);
    }

    @Test
    void consultarReservasDelAnfitrionExigeAnfitrion() {
        ReservaAnfitrionService servicio = mock(ReservaAnfitrionService.class);
        ReservaAnfitrionController controller = new ReservaAnfitrionController(servicio);

        assertThrows(AccesoNoPermitidoException.class,
                () -> controller.consultarReservas(usuario, conRol("ROLE_HUESPED")));
        verifyNoInteractions(servicio);
    }

    @Test
    void consultarElHistorialExigeHuesped() {
        ReservaHistorialService servicio = mock(ReservaHistorialService.class);
        ReservaHistorialController controller = new ReservaHistorialController(servicio);

        assertThrows(AccesoNoPermitidoException.class,
                () -> controller.consultarMisReservas(usuario, conRol("ROLE_ANFITRION")));
        verifyNoInteractions(servicio);
    }

    @Test
    void pagarUnaReservaExigeHuesped() {
        ReservaPagoSimuladoService servicio = mock(ReservaPagoSimuladoService.class);
        ReservaPagoSimuladoController controller = new ReservaPagoSimuladoController(servicio);

        assertThrows(AccesoNoPermitidoException.class,
                () -> controller.pagar(usuario, conRol("ROLE_ANFITRION"), idRecurso, "clave"));
        verifyNoInteractions(servicio);
    }

    @Test
    void exportarReportesExigeUnaSesionDeAdministrador() {
        ExportacionReporteService servicio = mock(ExportacionReporteService.class);
        UsuarioSesionQuery usuarios = mock(UsuarioSesionQuery.class);
        ExportacionReporteController controller = new ExportacionReporteController(servicio, usuarios);
        Authentication sinSesion = mock(Authentication.class);
        when(sinSesion.isAuthenticated()).thenReturn(false);
        Authentication huesped = conRol("ROLE_HUESPED");
        when(usuarios.consultarPorCorreo("alguien@ejemplo.com")).thenReturn(Optional.empty());

        assertThrows(AccesoNoPermitidoException.class, () -> controller.solicitar(null, null));
        assertThrows(AccesoNoPermitidoException.class, () -> controller.solicitar(null, sinSesion));
        assertThrows(AccesoNoPermitidoException.class, () -> controller.solicitar(null, huesped));
        verifyNoInteractions(servicio);
    }

    @Test
    void laExcepcionDeAccesoSeTraduceA403() {
        ResenaController controller = new ResenaController(mock(ResenaService.class));

        var respuesta = TraductorDeErroresPrueba.traducir(() -> controller.crear(usuario, null, idRecurso, null));

        assertEquals(HttpStatus.FORBIDDEN, respuesta.getStatusCode());
    }
}
