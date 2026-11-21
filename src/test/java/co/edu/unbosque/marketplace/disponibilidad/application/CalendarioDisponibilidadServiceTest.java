
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
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import co.edu.unbosque.marketplace.alojamiento.contracts.AlojamientoPublicacionQuery;

import co.edu.unbosque.marketplace.disponibilidad.application.dto.CalendarioDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.application.dto.ConsultaDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.application.dto.RangoDisponibleResponse;
import co.edu.unbosque.marketplace.disponibilidad.application.port.PeriodoDisponibilidadRepository;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;

import co.edu.unbosque.marketplace.publicacion.contracts.PublicacionDisponibilidadQuery;
import co.edu.unbosque.marketplace.reserva.contracts.NochesOcupadasQuery;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

@ExtendWith(MockitoExtension.class)
class CalendarioDisponibilidadServiceTest {

    @Mock
    private PeriodoDisponibilidadRepository periodoRepository;

    @Mock
    private NochesOcupadasQuery noches;

    @Mock
    private AlojamientoPublicacionQuery alojamientoQuery;

    @Mock
    private UsuarioPublicacionQuery usuarioQuery;

    @Mock
    private PublicacionDisponibilidadQuery publicacionQuery;

    private CalendarioDisponibilidadService servicio;

    private UUID idAlojamiento;
    private UUID idAnfitrion;
    private UUID idPeriodo;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @BeforeEach
    void preparar() {

        servicio = new CalendarioDisponibilidadService(
                periodoRepository,
                noches,
                alojamientoQuery,
                usuarioQuery,
                publicacionQuery
        );

        idAlojamiento = UUID.randomUUID();
        idAnfitrion = UUID.randomUUID();
        idPeriodo = UUID.randomUUID();

        fechaInicio = LocalDate.of(2026, 12, 1);
        fechaFin = LocalDate.of(2027, 1, 1);
    }

    // =====================================================
    // CONSULTA DE DISPONIBILIDAD POR FECHAS
    // =====================================================

    @Test
    void devuelveLosPeriodosHabilitadosDelRangoYLasNochesOcupadas() {

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(
                crearPeriodo(
                        LocalDate.of(2026, 10, 1),
                        LocalDate.of(2026, 11, 1),
                        true
                ),
                crearPeriodo(
                        LocalDate.of(2026, 11, 20),
                        LocalDate.of(2026, 12, 10),
                        true
                ),
                crearPeriodo(
                        LocalDate.of(2026, 12, 10),
                        LocalDate.of(2026, 12, 20),
                        false
                ),
                crearPeriodo(
                        LocalDate.of(2026, 12, 20),
                        LocalDate.of(2027, 2, 1),
                        true
                )
        ));

        LocalDate ocupada = LocalDate.of(2026, 12, 5);

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of(ocupada, ocupada));

        ConsultaDisponibilidadResponse respuesta =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
                );

        assertAll(
                () -> assertEquals(
                        idAlojamiento,
                        respuesta.idAlojamiento()
                ),
                () -> assertEquals(
                        List.of(
                                new RangoDisponibleResponse(
                                        LocalDate.of(2026, 11, 20),
                                        LocalDate.of(2026, 12, 10)
                                ),
                                new RangoDisponibleResponse(
                                        LocalDate.of(2026, 12, 20),
                                        LocalDate.of(2027, 2, 1)
                                )
                        ),
                        respuesta.periodosDisponibles()
                ),
                () -> assertEquals(
                        List.of(ocupada),
                        respuesta.nochesOcupadas()
                ),
                () -> assertFalse(
                        respuesta.cubiertoPorPeriodosDisponibles()
                ),
                () -> assertFalse(respuesta.disponible())
        );
    }

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

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
                );

        assertAll(
                () -> assertEquals(idAlojamiento, resultado.idAlojamiento()),
                () -> assertTrue(resultado.disponible()),
                () -> assertTrue(resultado.cubiertoPorPeriodosDisponibles()),
                () -> assertTrue(resultado.nochesOcupadas().isEmpty())
        );
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

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of(nocheOcupada));

        ConsultaDisponibilidadResponse resultado =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
                );

        assertAll(
                () -> assertFalse(resultado.disponible()),
                () -> assertTrue(resultado.cubiertoPorPeriodosDisponibles()),
                () -> assertEquals(
                        List.of(nocheOcupada),
                        resultado.nochesOcupadas()
                )
        );
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

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of(
                segunda,
                primera,
                segunda
        ));

        ConsultaDisponibilidadResponse resultado =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
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

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
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

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
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

        PeriodoDisponibilidad segundo = crearPeriodo(
                fechaInicio.plusDays(2),
                fechaFin,
                true
        );

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(primero, segundo));

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
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

        PeriodoDisponibilidad segundo = crearPeriodo(
                fechaInicio.plusDays(3),
                fechaFin,
                true
        );

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(primero, segundo));

        when(noches.nochesOcupadas(
                idAlojamiento,
                fechaInicio,
                fechaFin
        )).thenReturn(List.of());

        ConsultaDisponibilidadResponse resultado =
                servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
                );

        assertFalse(resultado.cubiertoPorPeriodosDisponibles());
        assertFalse(resultado.disponible());
    }

    // =====================================================
    // VALIDACIONES DE FECHAS
    // =====================================================

    @Test
    void debeRechazarIdentificadorDeAlojamientoNulo() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.consultarPorFechas(
                        null,
                        fechaInicio,
                        fechaFin
                )
        );

        verifyNoInteractions(publicacionQuery);
    }

    @Test
    void debeRechazarFechasNulas() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.consultarPorFechas(
                        idAlojamiento,
                        null,
                        fechaFin
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        null
                )
        );
    }

    @Test
    void debeRechazarFechaFinIgualAFechaInicio() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaInicio
                )
        );
    }

    @Test
    void debeRechazarFechaFinAnteriorAFechaInicio() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaInicio.minusDays(1)
                )
        );
    }

    @Test
    void noConsultaAlojamientosSinPublicacionActiva() {

        when(publicacionQuery.existePublicacionActivaPorAlojamiento(
                idAlojamiento
        )).thenReturn(false);

        assertThrows(
                NoSuchElementException.class,
                () -> servicio.consultarPorFechas(
                        idAlojamiento,
                        fechaInicio,
                        fechaFin
                )
        );

        verifyNoInteractions(periodoRepository, noches);
    }

    // =====================================================
    // CONSULTA DEL CALENDARIO COMPLETO
    // =====================================================

    @Test
    void debeConsultarCalendarioCompletoCorrectamente() {

        PeriodoDisponibilidad periodo = crearPeriodo(
                idPeriodo,
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

        when(noches.nochesOcupadas(idAlojamiento))
                .thenReturn(List.of(nocheOcupada));

        CalendarioDisponibilidadResponse resultado =
                servicio.consultarCalendarioCompleto(
                        idAnfitrion,
                        idAlojamiento
                );

        assertAll(
                () -> assertEquals(idAlojamiento, resultado.idAlojamiento()),
                () -> assertEquals(1, resultado.periodos().size()),
                () -> assertEquals(
                        idPeriodo,
                        resultado.periodos().get(0).idPeriodo()
                ),
                () -> assertEquals(
                        List.of(nocheOcupada),
                        resultado.nochesOcupadas()
                )
        );
    }

    @Test
    void debeRechazarAnfitrionNulo() {

        assertThrows(
                AccessDeniedException.class,
                () -> servicio.consultarCalendarioCompleto(
                        null,
                        idAlojamiento
                )
        );

        verifyNoInteractions(usuarioQuery);
    }

    @Test
    void debeRechazarIdentificadorDeAlojamientoNuloEnCalendario() {

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio.consultarCalendarioCompleto(
                        idAnfitrion,
                        null
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
                () -> servicio.consultarCalendarioCompleto(
                        idAnfitrion,
                        idAlojamiento
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
                () -> servicio.consultarCalendarioCompleto(
                        idAnfitrion,
                        idAlojamiento
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
                () -> servicio.consultarCalendarioCompleto(
                        idAnfitrion,
                        idAlojamiento
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
                () -> servicio.consultarCalendarioCompleto(
                        idAnfitrion,
                        idAlojamiento
                )
        );

        verifyNoInteractions(periodoRepository, noches);
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
                () -> servicio.consultarCalendarioCompleto(
                        idAnfitrion,
                        idAlojamiento
                )
        );

        verifyNoInteractions(periodoRepository, noches);
    }

    // =====================================================
    // MÉTODOS AUXILIARES
    // =====================================================

    private PeriodoDisponibilidad crearPeriodo(
            LocalDate inicio,
            LocalDate fin,
            boolean habilitado) {

        return crearPeriodo(
                UUID.randomUUID(),
                inicio,
                fin,
                habilitado
        );
    }

    private PeriodoDisponibilidad crearPeriodo(
            UUID id,
            LocalDate inicio,
            LocalDate fin,
            boolean habilitado) {

        return new PeriodoDisponibilidad(
                id,
                idAlojamiento,
                inicio,
                fin,
                habilitado,
                OffsetDateTime.now()
        );
    }
}
