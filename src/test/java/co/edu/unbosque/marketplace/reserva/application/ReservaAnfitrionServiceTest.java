package co.edu.unbosque.marketplace.reserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.compartido.domain.AccesoNoPermitidoException;
import co.edu.unbosque.marketplace.reserva.application.dto.ReservaAlojamientoResponse;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaAnfitrionDatos;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaRepository;
import co.edu.unbosque.marketplace.reserva.application.port.ReservaAnfitrionRepository;

@ExtendWith(MockitoExtension.class)
class ReservaAnfitrionServiceTest {

    private static final UUID ID_ANFITRION = UUID.randomUUID();
    private static final UUID ID_RESERVA = UUID.randomUUID();
    private static final OffsetDateTime LIMITE = OffsetDateTime.of(2026, 12, 1, 15, 0, 0, 0, ZoneOffset.UTC);

    @Mock
    private ReservaAnfitrionRepository historial;

    @Mock
    private ReservaRepository reservas;

    @InjectMocks
    private ReservaAnfitrionService servicio;

    private ReservaAnfitrionDatos pendiente() {
        return new ReservaAnfitrionDatos(ID_RESERVA, UUID.randomUUID(), "Cabaña", UUID.randomUUID(),
                "Hugo Pérez", LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12),
                new BigDecimal("820000"), "PENDIENTE_APROBACION", LIMITE);
    }

    @Test
    void incluyeElPlazoDeRespuestaEnElListado() {
        when(historial.consultarPorAnfitrion(ID_ANFITRION)).thenReturn(List.of(pendiente()));

        var respuesta = servicio.consultarReservas(ID_ANFITRION);

        assertEquals(1, respuesta.totalResultados());
        assertEquals(LIMITE, respuesta.reservas().get(0).retencionHasta());
        assertEquals("Hugo Pérez", respuesta.reservas().get(0).huesped());
    }

    @Test
    void incluyeElPlazoDeRespuestaEnElDetalle() {
        when(historial.consultarReservaPorAnfitrion(ID_ANFITRION, ID_RESERVA))
                .thenReturn(Optional.of(pendiente()));

        ReservaAlojamientoResponse respuesta = servicio.consultarReserva(ID_ANFITRION, ID_RESERVA);

        assertEquals(LIMITE, respuesta.retencionHasta());
        assertEquals("PENDIENTE_APROBACION", respuesta.estado());
    }

    @Test
    void niegaElAccesoAUnaReservaDeOtroAnfitrion() {
        when(historial.consultarReservaPorAnfitrion(ID_ANFITRION, ID_RESERVA)).thenReturn(Optional.empty());
        when(reservas.existsById(ID_RESERVA)).thenReturn(true);

        assertThrows(AccesoNoPermitidoException.class,
                () -> servicio.consultarReserva(ID_ANFITRION, ID_RESERVA));
    }
}
