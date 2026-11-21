package co.edu.unbosque.marketplace.auditoria.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import co.edu.unbosque.marketplace.auditoria.application.port.AuditoriaIndiceRepository;
import co.edu.unbosque.marketplace.auditoria.domain.AlmacenamientoInmutableAuditoria;

class AuditoriaWorkerSinBackblazeTest {

    private final ApplicationContextRunner contexto = new ApplicationContextRunner()
            .withPropertyValues("spring.profiles.active=worker")
            .withBean(AuditoriaOutboxService.class, () -> mock(AuditoriaOutboxService.class))
            .withBean(AuditoriaIndiceRepository.class, () -> mock(AuditoriaIndiceRepository.class))
            .withUserConfiguration(
                    AlmacenAuditoriaInmutableJdbc.class,
                    AuditoriaWorker.class,
                    ProgramadorAuditoria.class);

    @Test
    void sinBackblazeElWorkerArrancaYDejaLosEventosPendientes() {
        contexto.run(aplicacion -> {
            assertThat(aplicacion).hasNotFailed();
            assertThat(aplicacion).doesNotHaveBean(AuditoriaWorker.class);
            assertThat(aplicacion).doesNotHaveBean(ProgramadorAuditoria.class);
            assertThat(aplicacion).doesNotHaveBean(AlmacenAuditoriaInmutableJdbc.class);
        });
    }

    @Test
    void conBackblazeHabilitadoSeActivaLaCopiaDeAuditoria() {
        contexto.withPropertyValues("app.auditoria.b2.enabled=true")
                .withBean(AlmacenamientoInmutableAuditoria.class, () -> mock(AlmacenamientoInmutableAuditoria.class))
                .run(aplicacion -> {
                    assertThat(aplicacion).hasNotFailed();
                    assertThat(aplicacion).hasSingleBean(AuditoriaWorker.class);
                    assertThat(aplicacion).hasSingleBean(ProgramadorAuditoria.class);
                    assertThat(aplicacion).hasSingleBean(AlmacenAuditoriaInmutableJdbc.class);
                });
    }
}
