package co.edu.unbosque.marketplace.notificacion.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import co.edu.unbosque.marketplace.notificacion.application.NotificacionConsultaService;

@ExtendWith(MockitoExtension.class)
class NotificacionControllerTest {

    @Mock
    private NotificacionConsultaService consultaService;

    @InjectMocks
    private NotificacionController controller;

    private UUID idUsuario;

    @BeforeEach
    void setUp() {
        idUsuario = UUID.randomUUID();
    }

    @Test
    void consultarMisNotificacionesSinAutenticacion() {

        ResponseEntity<?> respuesta =
                controller.consultarMisNotificaciones(null);

        assertEquals(401, respuesta.getStatusCode().value());

        Map<?, ?> cuerpo = assertInstanceOf(
                Map.class,
                respuesta.getBody()
        );

        assertEquals(
                "Debes iniciar sesión para consultar tus notificaciones.",
                cuerpo.get("mensaje")
        );

        verifyNoInteractions(consultaService);
    }

    @Test
    void consultarMisNotificacionesCorrectamente() {

        when(consultaService.consultarMisNotificaciones(idUsuario))
                .thenReturn(List.of());

        ResponseEntity<?> respuesta =
                controller.consultarMisNotificaciones(idUsuario);

        assertEquals(200, respuesta.getStatusCode().value());
        assertEquals(List.of(), respuesta.getBody());

        verify(consultaService)
                .consultarMisNotificaciones(idUsuario);
    }
}
