package co.edu.unbosque.marketplace.disponibilidad.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;
import co.edu.unbosque.marketplace.disponibilidad.api.response.CalendarioDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.api.response.ConsultaDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.domain.NocheReservada;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.NocheReservadaRepository;
import co.edu.unbosque.marketplace.disponibilidad.infrastructure.persistence.PeriodoDisponibilidadRepository;
import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

class CalendarioDisponibilidadServiceTest {

    private PeriodoDisponibilidadRepository periodoRepository;
    private NocheReservadaRepository nocheReservadaRepository;
    private AlojamientoPublicacionQuery alojamientoQuery;
    private UsuarioPublicacionQuery usuarioQuery;
    private PublicacionDisponibilidadQuery publicacionQuery;

    private CalendarioDisponibilidadService service;

    private UUID idAnfitrion;
    private UUID idAlojamiento;
    private UUID idPeriodo;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @BeforeEach
    void setUp() {
        periodoRepository = mock(PeriodoDisponibilidadRepository.class);
        nocheReservadaRepository = mock(NocheReservadaRepository.class);
        alojamientoQuery = mock(AlojamientoPublicacionQuery.class);
        usuarioQuery = mock(UsuarioPublicacionQuery.class);
        publicacionQuery = mock(PublicacionDisponibilidadQuery.class);

        service = new CalendarioDisponibilidadService(
                periodoRepository,
                nocheReservadaRepository,
                alojamientoQuery,
                usuarioQuery,
                publicacionQuery
        );

        idAnfitrion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        idPeriodo = UUID.randomUUID();

        fechaInicio = LocalDate.of(2026, 12, 10);
        fechaFin = LocalDate.of(2026, 12, 15);
    }

    private PeriodoDisponibilidad crearPeriodo(
            LocalDate inicio,
            LocalDate fin,
            boolean habilitado) {

        return new PeriodoDisponibilidad(
                idPeriodo,
                idAlojamiento,
                inicio,
                fin,
                habilitado,
                OffsetDateTime.now()
        );
    }

    private NocheReservada crearNoche(LocalDate fecha) {
        return new NocheReservada(
                UUID.randomUUID(),
                idAlojamiento,
                fecha
        );
    }

    // CONSULTAR DISPONIBILIDAD POR FECHAS

    @Test
    void debeConsultarDisponibilidadCuandoElRangoEstaCubierto() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                fechaInicio,
                fechaFin,
                true
        );

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(periodo));

        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                );

        assertEquals(idAlojamiento, resultado.idAlojamiento());
        assertTrue(resultado.disponible());
        assertTrue(resultado.cubiertoPorPeriodosDisponibles());
        assertTrue(resultado.nochesOcupadas().isEmpty());
    }

    @Test
    void debeRetornarNoDisponibleCuandoExistenNochesReservadas() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                fechaInicio,
                fechaFin,
                true
        );

        LocalDate nocheOcupada = fechaInicio.plusDays(2);

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(periodo));

        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(List.of(
                crearNoche(nocheOcupada)
        ));

        ConsultaDisponibilidadResponse resultado =
                service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                );

        assertFalse(resultado.disponible());
        assertTrue(resultado.cubiertoPorPeriodosDisponibles());
        assertEquals(List.of(nocheOcupada), resultado.nochesOcupadas());
    }

    @Test
    void debeOrdenarYEliminarNochesReservadasRepetidas() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                fechaInicio,
                fechaFin,
                true
        );

        LocalDate primera = fechaInicio.plusDays(1);
        LocalDate segunda = fechaInicio.plusDays(3);

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(periodo));

        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(List.of(
                crearNoche(segunda),
                crearNoche(primera),
                crearNoche(segunda)
        ));

        ConsultaDisponibilidadResponse resultado =
                service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                );

        assertEquals(
                List.of(primera, segunda),
                resultado.nochesOcupadas()
        );
        assertFalse(resultado.disponible());
    }

    @Test
    void debeRetornarNoCubiertoCuandoNoExistenPeriodos() {
        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of());

        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                );

        assertFalse(resultado.disponible());
        assertFalse(resultado.cubiertoPorPeriodosDisponibles());
    }

    @Test
    void debeIgnorarPeriodosDeshabilitados() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                fechaInicio,
                fechaFin,
                false
        );

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(periodo));

        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                );

        assertFalse(resultado.disponible());
        assertFalse(resultado.cubiertoPorPeriodosDisponibles());
    }

    @Test
    void debeReconocerCoberturaConPeriodosConsecutivos() {
        PeriodoDisponibilidad primero = crearPeriodo(
                fechaInicio,
                fechaInicio.plusDays(2),
                true
        );

        PeriodoDisponibilidad segundo = new PeriodoDisponibilidad(
                UUID.randomUUID(),
                idAlojamiento,
                fechaInicio.plusDays(2),
                fechaFin,
                true,
                OffsetDateTime.now()
        );

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(primero, segundo));

        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                );

        assertTrue(resultado.cubiertoPorPeriodosDisponibles());
        assertTrue(resultado.disponible());
    }

    @Test
    void debeRetornarNoCubiertoCuandoExisteUnIntervaloSinDisponibilidad() {
        PeriodoDisponibilidad primero = crearPeriodo(
                fechaInicio,
                fechaInicio.plusDays(2),
                true
        );

        PeriodoDisponibilidad segundo = new PeriodoDisponibilidad(
                UUID.randomUUID(),
                idAlojamiento,
                fechaInicio.plusDays(3),
                fechaFin,
                true,
                OffsetDateTime.now()
        );

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(primero, segundo));

        when(nocheReservadaRepository.consultarNochesOcupadasEnRango(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                );

        assertFalse(resultado.cubiertoPorPeriodosDisponibles());
        assertFalse(resultado.disponible());
    }

    @Test
    void debeRechazarIdentificadorDeAlojamientoNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarPorFechas(
                        null, fechaInicio, fechaFin
                )
        );

        verifyNoInteractions(publicacionQuery);
    }

    @Test
    void debeRechazarFechasNulas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarPorFechas(
                        idAlojamiento, null, fechaFin
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarPorFechas(
                        idAlojamiento, fechaInicio, null
                )
        );
    }

    @Test
    void debeRechazarFechaFinIgualAFechaInicio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaInicio
                )
        );
    }

    @Test
    void debeRechazarFechaFinAnteriorAFechaInicio() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaInicio.minusDays(1)
                )
        );
    }

    @Test
    void debeRechazarAlojamientoSinPublicacionActiva() {
        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(false);

        assertThrows(
                NoSuchElementException.class,
                () -> service.consultarPorFechas(
                        idAlojamiento, fechaInicio, fechaFin
                )
        );

        verifyNoInteractions(periodoRepository);
        verifyNoInteractions(nocheReservadaRepository);
    }

    // CONSULTAR CALENDARIO COMPLETO

    @Test
    void debeConsultarCalendarioCompletoCorrectamente() {
        PeriodoDisponibilidad periodo = crearPeriodo(
                fechaInicio,
                fechaFin,
                true
        );

        LocalDate nocheOcupada = fechaInicio.plusDays(2);

        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(
                        new UsuarioPublicacionInfo(
                                true, true, true, false, true, 0
                        )
                ));

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(periodo));

        when(nocheReservadaRepository.consultarTodasLasNochesOcupadas(
                idAlojamiento
        )).thenReturn(List.of(crearNoche(nocheOcupada)));

        CalendarioDisponibilidadResponse resultado =
                service.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                );

        assertEquals(idAlojamiento, resultado.idAlojamiento());
        assertEquals(1, resultado.periodos().size());
        assertEquals(idPeriodo, resultado.periodos().get(0).idPeriodo());
        assertEquals(List.of(nocheOcupada), resultado.nochesOcupadas());
    }

    @Test
    void debeRechazarAnfitrionNulo() {
        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarCalendarioCompleto(
                        null, idAlojamiento
                )
        );

        verifyNoInteractions(usuarioQuery);
    }

    @Test
    void debeRechazarIdentificadorDeAlojamientoNuloEnCalendario() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarCalendarioCompleto(
                        idAnfitrion, null
                )
        );

        verifyNoInteractions(usuarioQuery);
    }

    @Test
    void debeRechazarUsuarioQueNoExiste() {
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(alojamientoQuery);
    }

    @Test
    void debeRechazarUsuarioQueNoEsAnfitrion() {
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(
                        new UsuarioPublicacionInfo(
                                false, true, true, false, true, 0
                        )
                ));

        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(alojamientoQuery);
    }

    @Test
    void debeRechazarCuentaNoHabilitadaParaPublicar() {
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(
                        new UsuarioPublicacionInfo(
                                true, false, true, false, true, 0
                        )
                ));

        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(alojamientoQuery);
    }

    @Test
    void debeRechazarAlojamientoInexistente() {
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(
                        new UsuarioPublicacionInfo(
                                true, true, true, false, true, 0
                        )
                ));

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> service.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(periodoRepository);
        verifyNoInteractions(nocheReservadaRepository);
    }

    @Test
    void debeRechazarAnfitrionQueNoEsPropietario() {
        UUID otroAnfitrion = UUID.randomUUID();

        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(
                        new UsuarioPublicacionInfo(
                                true, true, true, false, true, 0
                        )
                ));

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(otroAnfitrion));

        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarCalendarioCompleto(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(periodoRepository);
        verifyNoInteractions(nocheReservadaRepository);
    }
}
