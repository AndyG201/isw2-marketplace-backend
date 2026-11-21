package co.edu.unbosque.marketplace.usuario.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.marketplace.auditoria.contracts.AuditoriaEventoCommand;
import co.edu.unbosque.marketplace.config.PasswordConfig;
import co.edu.unbosque.marketplace.publicacion.api.PublicacionAdministrativaDetalleController;
import co.edu.unbosque.marketplace.publicacion.api.PublicacionModeracionController;
import co.edu.unbosque.marketplace.publicacion.application.ModeracionPublicacionService;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionDetallePublicoService;
import co.edu.unbosque.marketplace.reporte.api.DashboardIndicadoresController;
import co.edu.unbosque.marketplace.reporte.application.DashboardIndicadoresService;
import co.edu.unbosque.marketplace.usuario.api.AdministradorUsuarioController;
import co.edu.unbosque.marketplace.usuario.application.AdministradorService;
import co.edu.unbosque.marketplace.usuario.domain.EstadoUsuario;
import co.edu.unbosque.marketplace.usuario.domain.Rol;
import co.edu.unbosque.marketplace.usuario.domain.Usuario;

@WebMvcTest(useDefaultFilters = false)
@Import({
        SecurityConfig.class,
        PasswordConfig.class,
        JwtAuthenticationFilter.class,
        AdministradorUsuarioController.class,
        PublicacionModeracionController.class,
        PublicacionAdministrativaDetalleController.class,
        DashboardIndicadoresController.class
})
class AccesoAuditorOperacionTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdministradorService administradorService;

    @MockitoBean
    private ModeracionPublicacionService moderacionService;

    @MockitoBean
    private PublicacionDetallePublicoService detalleService;

    @MockitoBean
    private DashboardIndicadoresService dashboardService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuditoriaEventoCommand auditoriaEventoCommand;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private void sesionCon(String rol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(UUID.randomUUID());
        usuario.setCorreo("persona@test.co");
        usuario.setRol(new Rol(UUID.randomUUID(), rol, null));
        usuario.setEstado(EstadoUsuario.ACTIVA);
        usuario.setCorreoVerificado(true);
        when(jwtUtil.extractUsername(TOKEN)).thenReturn("persona@test.co");
        when(userDetailsService.loadUserByUsername("persona@test.co")).thenReturn(usuario);
        when(jwtUtil.validateToken(eq(TOKEN), any())).thenReturn(true);
    }

    @Test
    void elAuditorConsultaUsuariosPublicacionesEIndicadores() throws Exception {
        sesionCon("AUDITOR_OPERACION");
        when(administradorService.consultarUsuarios()).thenReturn(List.of());
        when(moderacionService.consultarPublicaciones()).thenReturn(List.of());

        mockMvc.perform(get("/administracion/usuarios").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(get("/administracion/publicaciones").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(get("/administracion/dashboard/indicadores")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void elAuditorNoModeraNiAbreElDetalleAdministrativo() throws Exception {
        sesionCon("AUDITOR_OPERACION");
        UUID id = UUID.randomUUID();

        mockMvc.perform(patch("/administracion/usuarios/{id}/restringir", id)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Prueba\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/administracion/publicaciones/{id}/bloquear", id)
                .header("Authorization", "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"Prueba\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/administracion/publicaciones/{id}", id)
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(administradorService, moderacionService, detalleService);
    }

    @Test
    void otrosRolesNoVenLasListasNiLosIndicadores() throws Exception {
        sesionCon("ANFITRION");

        mockMvc.perform(get("/administracion/usuarios").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/administracion/publicaciones").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/administracion/dashboard/indicadores")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());

        verifyNoInteractions(administradorService, moderacionService, dashboardService);
    }

    @Test
    void elAdministradorSigueConsultandoTodo() throws Exception {
        sesionCon("ADMINISTRADOR");
        when(administradorService.consultarUsuarios()).thenReturn(List.of());

        mockMvc.perform(get("/administracion/usuarios").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
        mockMvc.perform(get("/administracion/dashboard/indicadores")
                .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
    }
}
