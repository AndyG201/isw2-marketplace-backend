package co.edu.unbosque.marketplace.reserva.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.reserva.domain.Reserva;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaHistorialDatos;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaHistorialRepository;
import co.edu.unbosque.marketplace.reserva.infrastructure.persistence.ReservaRepository;

@ExtendWith(MockitoExtension.class)
class ReservaHistorialServiceTest {

    private static final UUID ID_HUESPED = UUID.randomUUID();
    private static final UUID ID_RESERVA = UUID.randomUUID();

    @Mock
    private ReservaHistorialRepository historial;

    @Mock
    private ReservaRepository reservas;

    @InjectMocks
    private ReservaHistorialService servicio;

    private ReservaHistorialDatos datos(String estado, boolean resenada) {
        return new ReservaHistorialDatos(ID_RESERVA, UUID.randomUUID(), "Loft", LocalDate.of(2026, 12, 1),
                LocalDate.of(2026, 12, 4), new BigDecimal("624000"), estado, null, null, resenada);
    }

    @Test
    void indicaSiCadaReservaYaTieneResena() {
        when(historial.consultarPorHuesped(ID_HUESPED))
                .thenReturn(List.of(datos("COMPLETADA", true), datos("CONFIRMADA", false)));

        var respuesta = servicio.consultarMisReservas(ID_HUESPED);

        assertEquals(2, respuesta.totalResultados());
        assertTrue(respuesta.reservas().get(0).resenada());
        assertFalse(respuesta.reservas().get(1).resenada());
    }

    @Test
    void incluyeLaResenaEnElDetalleDeUnaReservaPropia() {
        Reserva reserva = new Reserva();
        reserva.setIdHuesped(ID_HUESPED);
        when(reservas.findById(ID_RESERVA)).thenReturn(Optional.of(reserva));
        when(historial.consultarPorId(ID_RESERVA)).thenReturn(Optional.of(datos("COMPLETADA", true)));

        var respuesta = servicio.consultarMiReserva(ID_HUESPED, ID_RESERVA);

        assertEquals("COMPLETADA", respuesta.estado());
        assertTrue(respuesta.resenada());
    }
}
