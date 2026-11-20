package co.edu.unbosque.marketplace.resena.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
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
import co.edu.unbosque.marketplace.resena.api.response.ResenasPublicacionResponse;
import co.edu.unbosque.marketplace.resena.application.ResenaPublicaQueryService;
import co.edu.unbosque.marketplace.resena.application.ResenaService;
import co.edu.unbosque.marketplace.usuario.security.JwtAuthenticationFilter;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.SecurityConfig;

@WebMvcTest(useDefaultFilters = false)
@Import({
        SecurityConfig.class,
        PasswordConfig.class,
        JwtAuthenticationFilter.class,
        ResenaPublicaController.class,
        ResenaController.class
})
class ResenaPublicaAccesoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResenaPublicaQueryService consultaResenas;

    @MockitoBean
    private ResenaService resenaService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuditoriaEventoCommand auditoriaEventoCommand;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void permiteVerLasResenasDeUnaPublicacionSinIniciarSesion() throws Exception {
        UUID id = UUID.randomUUID();
        when(consultaResenas.consultar(id)).thenReturn(new ResenasPublicacionResponse(
                id, List.of(), 0, BigDecimal.ZERO, "Esta publicación aún no tiene reseñas visibles."));

        mockMvc.perform(get("/publicaciones/{id}/resenas", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResenas").value(0));
    }

    @Test
    void crearUnaResenaSiguePidiendoSesion() throws Exception {
        mockMvc.perform(post("/reservas/{id}/resenas", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"calificacion\":5,\"comentario\":\"Excelente\"}"))
                .andExpect(status().isUnauthorized());
    }
}
