package co.edu.unbosque.marketplace.notificacion.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import co.edu.unbosque.marketplace.notificacion.application.NotificacionConsultaService;

@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionConsultaService consultaService;

    private UUID idUsuario;

    @BeforeEach
    void setUp() {
        idUsuario = UUID.randomUUID();
    }

    @Test
    void consultarMisNotificacionesSinAutenticacion()
            throws Exception {

        mockMvc.perform(get("/notificaciones/mis-notificaciones"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value(
                        "Debes iniciar sesión para consultar tus notificaciones."
                ));

        verifyNoInteractions(consultaService);
    }

    @Test
    void consultarMisNotificacionesCorrectamente()
            throws Exception {

        when(consultaService.consultarMisNotificaciones(idUsuario))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/notificaciones/mis-notificaciones")
                        .with(request -> {
                            request.setUserPrincipal(() -> idUsuario.toString());
                            return request;
                        })
        )
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(consultaService).consultarMisNotificaciones(idUsuario);
    }
}
