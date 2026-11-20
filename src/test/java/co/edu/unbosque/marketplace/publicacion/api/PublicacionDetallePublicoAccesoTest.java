package co.edu.unbosque.marketplace.publicacion.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.marketplace.config.PasswordConfig;
import co.edu.unbosque.marketplace.publicacion.application.PublicacionDetallePublicoService;
import co.edu.unbosque.marketplace.servicio.api.ServicioController;
import co.edu.unbosque.marketplace.servicio.application.ServicioService;
import co.edu.unbosque.marketplace.usuario.security.JwtAuthenticationFilter;
import co.edu.unbosque.marketplace.usuario.security.JwtUtil;
import co.edu.unbosque.marketplace.usuario.security.SecurityConfig;

@WebMvcTest(useDefaultFilters = false)
@Import({
        SecurityConfig.class,
        PasswordConfig.class,
        JwtAuthenticationFilter.class,
        PublicacionDetallePublicoController.class,
        ServicioController.class
})
class PublicacionDetallePublicoAccesoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublicacionDetallePublicoService detallePublicoService;

    @MockitoBean
    private ServicioService servicioService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void permiteVerElDetallePublicoSinIniciarSesion() throws Exception {
        UUID id = UUID.randomUUID();
        when(detallePublicoService.consultar(id))
                .thenThrow(new NoSuchElementException("La publicación no existe o no está disponible."));

        mockMvc.perform(get("/publicaciones/{id}/detalle-publico", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La publicación no existe o no está disponible."));
    }

    @Test
    void permiteVerElCatalogoDeServiciosSinIniciarSesion() throws Exception {
        when(servicioService.consultarServicios()).thenReturn(List.of());

        mockMvc.perform(get("/servicios"))
                .andExpect(status().isOk());
    }

    @Test
    void lasPoliticasActivasSiguenPidiendoSesion() throws Exception {
        mockMvc.perform(get("/politicas-cancelacion/activas"))
                .andExpect(status().isUnauthorized());
    }
}
