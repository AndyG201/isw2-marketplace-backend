package co.edu.unbosque.marketplace.trabajo.application;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import co.edu.unbosque.marketplace.compartido.domain.ConflictoNegocioException;
import co.edu.unbosque.marketplace.compartido.domain.FallaInternaException;
import co.edu.unbosque.marketplace.reserva.application.ReservaAprobacionRetencionScheduler;
import co.edu.unbosque.marketplace.reserva.application.ReservaPagoConciliacionWorker;
import co.edu.unbosque.marketplace.reserva.application.ReservaRetencionScheduler;
import co.edu.unbosque.marketplace.reserva.application.ReservaTrabajosProcessor;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaCancelacionJdbcRepository;
import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;
import co.edu.unbosque.marketplace.trabajo.TipoTrabajo;

/** Fallas internas del procesamiento en segundo plano: un trabajo sin procesador y una cancelación concurrente. */
class ProcesadoresDeTrabajoExcepcionesTest {

    @Test
    void elWorkerFallaSiNingunProcesadorSoportaElTrabajo() {
        ColaTrabajos cola = mock(ColaTrabajos.class);
        when(cola.tomarSiguiente()).thenReturn(Optional.of(new ColaTrabajos.Trabajo(TipoTrabajo.COPIAR_AUDITORIA, "x")));
        WorkerTrabajos worker = new WorkerTrabajos(cola, List.of());

        assertThrows(FallaInternaException.class, worker::procesarSiguiente);
    }

    @Test
    void elProcesadorDeReservasNoAdmiteOtrosTrabajos() {
        ReservaTrabajosProcessor procesador = new ReservaTrabajosProcessor(
                mock(ReservaPagoConciliacionWorker.class),
                mock(ReservaAprobacionRetencionScheduler.class),
                mock(ReservaRetencionScheduler.class));

        assertThrows(FallaInternaException.class, () -> procesador.procesar(TipoTrabajo.EXPORTAR_REPORTE, "x"));
    }

    @Test
    void cancelarUnaReservaQueCambioDeEstadoEsUnConflicto() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        ReservaCancelacionJdbcRepository repositorio = new ReservaCancelacionJdbcRepository(jdbc);

        // Sin filas actualizadas: otra operación cambió el estado de la reserva.
        assertThrows(ConflictoNegocioException.class, () -> repositorio.marcarCancelada(UUID.randomUUID()));
    }
}
