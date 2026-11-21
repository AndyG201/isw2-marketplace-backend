package co.edu.unbosque.marketplace.auditoria.api;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.auditoria.application.AuditoriaConsultaService;
import co.edu.unbosque.marketplace.auditoria.application.dto.EventoAuditoriaResponse;
import co.edu.unbosque.marketplace.auditoria.application.dto.PaginaAuditoriaResponse;

@ExtendWith(MockitoExtension.class)
class AuditoriaConsultaControllerTest {

    @Mock
    private AuditoriaConsultaService service;

    @InjectMocks
    private AuditoriaConsultaController controller;

    @Test
    void consultaDelegaFiltrosYPaginacionAlServicio() {
        UUID userId = UUID.randomUUID();
        OffsetDateTime from = OffsetDateTime.parse("2026-11-01T00:00:00Z");
        OffsetDateTime to = OffsetDateTime.parse("2026-11-20T23:59:59Z");
        PaginaAuditoriaResponse expected = new PaginaAuditoriaResponse(List.of(), 1, 10, 0, 0, "vacío");
        when(service.consultar(userId, from, to, "LOGIN", "EXITOSO", "USUARIO", 1, 10))
                .thenReturn(expected);

        PaginaAuditoriaResponse actual = controller.consultar(
                userId, from, to, "LOGIN", "EXITOSO", "USUARIO", 1, 10);

        assertSame(expected, actual);
        verify(service).consultar(userId, from, to, "LOGIN", "EXITOSO", "USUARIO", 1, 10);
    }

    @Test
    void consultaPorIdDelegaElUuidAlServicio() {
        UUID eventId = UUID.randomUUID();
        EventoAuditoriaResponse expected = new EventoAuditoriaResponse(
                eventId, null, OffsetDateTime.now(), "LOGIN", "EXITOSO", "USUARIO",
                null, "{}", "corr-1");
        when(service.consultar(eventId)).thenReturn(expected);

        assertSame(expected, controller.consultarPorId(eventId));
        verify(service).consultar(eventId);
    }
}