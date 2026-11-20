package co.edu.unbosque.marketplace.disponibilidad.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.disponibilidad.api.response.ConsultaDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.api.response.RangoDisponibleResponse;
import co.edu.unbosque.marketplace.disponibilidad.domain.NocheReservada;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.NocheReservadaRepository;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.PeriodoDisponibilidadRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
class CalendarioDisponibilidadServiceTest {

    private static final UUID ALOJAMIENTO = UUID.randomUUID();
    private static final LocalDate INICIO = LocalDate.of(2026, 12, 1);
    private static final LocalDate FIN = LocalDate.of(2027, 1, 1);

    @Mock
    private PeriodoDisponibilidadRepository periodoRepository;

    @Mock
    private NocheReservadaRepository nocheReservadaRepository;

    @Mock
    private AlojamientoPublicacionQuery alojamientoQuery;

    @Mock
    private UsuarioPublicacionQuery usuarioQuery;

    @Mock
    private PublicacionDisponibilidadQuery publicacionQuery;

    private CalendarioDisponibilidadService servicio;

    @BeforeEach
    void preparar() {
        servicio = new CalendarioDisponibilidadService(
                periodoRepository, nocheReservadaRepository, alojamientoQuery, usuarioQuery, publicacionQuery);
    }

    private PeriodoDisponibilidad periodo(LocalDate inicio, LocalDate fin, boolean habilitado) {
        return new PeriodoDisponibilidad(
                UUID.randomUUID(), ALOJAMIENTO, inicio, fin, habilitado, OffsetDateTime.now());
    }

    @Test
    void devuelveLosPeriodosHabilitadosDelRangoYLasNochesOcupadas() {
        when(publicacionQuery.existePublicacionActivaPorAlojamiento(ALOJAMIENTO)).thenReturn(true);
        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(ALOJAMIENTO)).thenReturn(List.of(
                periodo(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 11, 1), true),
                periodo(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 10), true),
                periodo(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 20), false),
                periodo(LocalDate.of(2026, 12, 20), LocalDate.of(2027, 2, 1), true)));
        LocalDate ocupada = LocalDate.of(2026, 12, 5);
        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(ALOJAMIENTO, INICIO, FIN))
                .thenReturn(List.of(
                        new NocheReservada(UUID.randomUUID(), ALOJAMIENTO, ocupada),
                        new NocheReservada(UUID.randomUUID(), ALOJAMIENTO, ocupada)));

        ConsultaDisponibilidadResponse respuesta = servicio.consultarPorFechas(ALOJAMIENTO, INICIO, FIN);

        assertEquals(List.of(
                new RangoDisponibleResponse(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 10)),
                new RangoDisponibleResponse(LocalDate.of(2026, 12, 20), LocalDate.of(2027, 2, 1))),
                respuesta.periodosDisponibles());
        assertEquals(List.of(ocupada), respuesta.nochesOcupadas());
        assertFalse(respuesta.cubiertoPorPeriodosDisponibles());
        assertFalse(respuesta.disponible());
    }

    @Test
    void unRangoCubiertoYLibreEstaDisponible() {
        when(publicacionQuery.existePublicacionActivaPorAlojamiento(ALOJAMIENTO)).thenReturn(true);
        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(ALOJAMIENTO))
                .thenReturn(List.of(periodo(LocalDate.of(2026, 11, 1), LocalDate.of(2027, 3, 1), true)));
        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(ALOJAMIENTO, INICIO, FIN))
                .thenReturn(List.of());

        ConsultaDisponibilidadResponse respuesta = servicio.consultarPorFechas(ALOJAMIENTO, INICIO, FIN);

        assertTrue(respuesta.disponible());
        assertEquals(1, respuesta.periodosDisponibles().size());
    }

    @Test
    void noConsultaAlojamientosSinPublicacionActiva() {
        when(publicacionQuery.existePublicacionActivaPorAlojamiento(ALOJAMIENTO)).thenReturn(false);

        assertThrows(NoSuchElementException.class,
                () -> servicio.consultarPorFechas(ALOJAMIENTO, INICIO, FIN));
    }

    @Test
    void validaElRangoDeFechas() {
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarPorFechas(ALOJAMIENTO, FIN, INICIO));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.consultarPorFechas(null, INICIO, FIN));
    }
}
