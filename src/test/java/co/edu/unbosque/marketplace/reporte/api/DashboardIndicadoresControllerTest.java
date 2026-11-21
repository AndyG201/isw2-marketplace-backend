package co.edu.unbosque.marketplace.reporte.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import co.edu.unbosque.marketplace.reporte.api.response.DashboardIndicadoresResponse;
import co.edu.unbosque.marketplace.reporte.application.DashboardIndicadoresService;

class DashboardIndicadoresControllerTest {

    private final DashboardIndicadoresService dashboard = mock(DashboardIndicadoresService.class);
    private final DashboardIndicadoresController controller = new DashboardIndicadoresController(dashboard);

    private static Authentication sesion(String rol) {
        return new UsernamePasswordAuthenticationToken(
                "persona@test.co", null, List.of(new SimpleGrantedAuthority(rol)));
    }

    @Test
    void elAdministradorYElAuditorConsultanLosIndicadores() {
        DashboardIndicadoresResponse indicadores = mock(DashboardIndicadoresResponse.class);
        when(dashboard.consultar()).thenReturn(indicadores);

        for (String rol : List.of("ROLE_ADMINISTRADOR", "ROLE_AUDITOR_OPERACION")) {
            var respuesta = controller.consultar(sesion(rol));

            assertEquals(HttpStatus.OK, respuesta.getStatusCode());
            assertSame(indicadores, respuesta.getBody());
        }
    }

    @Test
    void rechazaOtrosRolesYPeticionesSinSesion() {
        Map<String, String> mensaje = Map.of("mensaje",
                "Solo un administrador o un auditor de operación puede consultar el dashboard.");

        var anfitrion = controller.consultar(sesion("ROLE_ANFITRION"));
        var sinSesion = controller.consultar(null);

        assertEquals(HttpStatus.FORBIDDEN, anfitrion.getStatusCode());
        assertEquals(mensaje, anfitrion.getBody());
        assertEquals(HttpStatus.FORBIDDEN, sinSesion.getStatusCode());
        assertEquals(mensaje, sinSesion.getBody());
        verifyNoInteractions(dashboard);
    }
}
