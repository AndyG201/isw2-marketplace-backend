package co.edu.unbosque.marketplace.auditoria.application;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class ProgramadorAuditoriaTest {

    @Mock
    private AuditoriaOutboxService outbox;

    @InjectMocks
    private ProgramadorAuditoria programador;

    @Test
    void programaLosEventosPendientesDelegandoEnLaOutbox() {
        programador.programarPendientes();
        verify(outbox).encolarPendientes();
    }

    @Test
    void soloSeActivaEnPerfilWorkerYDeclaraIntervalo() throws Exception {
        Profile profile = ProgramadorAuditoria.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertEquals("worker", profile.value()[0]);

        Scheduled scheduled = ProgramadorAuditoria.class
                .getMethod("programarPendientes")
                .getAnnotation(Scheduled.class);
        assertNotNull(scheduled);
        assertEquals("${app.auditoria.salida.intervalo-ms:2000}", scheduled.fixedDelayString());
    }
}