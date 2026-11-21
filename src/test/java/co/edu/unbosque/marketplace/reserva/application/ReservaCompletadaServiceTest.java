package co.edu.unbosque.marketplace.reserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class ReservaCompletadaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 12, 5);

    @Mock
    private ReservaRepository reservas;

    @Mock
    private ReservaCompletadaService servicioProgramado;

    @InjectMocks
    private ReservaCompletadaService servicio;

    private Reserva confirmada() {
        return new Reserva(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "INMEDIATA", "CONFIRMADA", null, "clave", false,
                OffsetDateTime.of(2026, 11, 20, 10, 0, 0, 0, ZoneOffset.UTC), null);
    }

    @Test
    void marcaComoCompletadasLasReservasConfirmadasCuyaSalidaYaPaso() {
        Reserva primera = confirmada();
        Reserva segunda = confirmada();
        List<Reserva> finalizadas = List.of(primera, segunda);
        when(reservas.buscarConfirmadasFinalizadas(HOY)).thenReturn(finalizadas);

        int completadas = servicio.completarReservasFinalizadas(HOY);

        assertEquals(2, completadas);
        assertEquals("COMPLETADA", primera.getEstado());
        assertEquals("COMPLETADA", segunda.getEstado());
        verify(reservas).saveAllAndFlush(finalizadas);
    }

    @Test
    void noModificaNadaCuandoNoHayReservasFinalizadas() {
        when(reservas.buscarConfirmadasFinalizadas(HOY)).thenReturn(List.of());

        assertEquals(0, servicio.completarReservasFinalizadas(HOY));
        verify(reservas).saveAllAndFlush(List.of());
    }

    @Test
    void elProgramadorUsaLaFechaDeColombia() {
        Instant instante = Instant.parse("2026-12-06T03:00:00Z");
        Clock reloj = Clock.fixed(instante, ZoneId.of("America/Bogota"));

        new ReservaCompletadaScheduler(servicioProgramado, reloj).completarReservasFinalizadas();

        verify(servicioProgramado).completarReservasFinalizadas(HOY);
    }

    @Test
    void elProgramadorPorDefectoUsaElRelojDelSistema() {
        new ReservaCompletadaScheduler(servicioProgramado).completarReservasFinalizadas();

        verify(servicioProgramado).completarReservasFinalizadas(LocalDate.now(ZoneId.of("America/Bogota")));
    }
}
