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
import co.edu.unbosque.marketplace.disponibilidad.application.dto.PeriodoDisponibilidadRequest;
import co.edu.unbosque.marketplace.disponibilidad.application.dto.PeriodoDisponibilidadResponse;
import co.edu.unbosque.marketplace.disponibilidad.domain.PeriodoDisponibilidad;
import co.edu.unbosque.marketplace.reserva.contracts.NochesOcupadasQuery;
import co.edu.unbosque.marketplace.disponibilidad.application.port.PeriodoDisponibilidadRepository;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionInfo;
import co.edu.unbosque.marketplace.usuario.contracts.UsuarioPublicacionQuery;

class PeriodoDisponibilidadServiceTest {

    private PeriodoDisponibilidadRepository periodoRepository;
    private NochesOcupadasQuery noches;
    private AlojamientoPublicacionQuery alojamientoQuery;
    private UsuarioPublicacionQuery usuarioQuery;

    private PeriodoDisponibilidadService service;

    private UUID idAnfitrion;
    private UUID idAlojamiento;
    private UUID idPeriodo;
    private UUID otroAnfitrion;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private PeriodoDisponibilidadRequest solicitud;

    @BeforeEach
    void setUp() {
        periodoRepository = mock(PeriodoDisponibilidadRepository.class);
        noches = mock(NochesOcupadasQuery.class);
        alojamientoQuery = mock(AlojamientoPublicacionQuery.class);
        usuarioQuery = mock(UsuarioPublicacionQuery.class);

        service = new PeriodoDisponibilidadService(
                periodoRepository,
                noches,
                alojamientoQuery,
                usuarioQuery
        );

        idAnfitrion = UUID.randomUUID();
        idAlojamiento = UUID.randomUUID();
        idPeriodo = UUID.randomUUID();
        otroAnfitrion = UUID.randomUUID();

        fechaInicio = LocalDate.of(2026, 12, 10);
        fechaFin = LocalDate.of(2026, 12, 15);

        solicitud = crearSolicitud(fechaInicio, fechaFin);

        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(
                        new UsuarioPublicacionInfo(
                                true, true, true, false, true, 0
                        )
                ));

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));

        when(noches.hayNochesOcupadas(
                any(UUID.class),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(false);

        when(periodoRepository.existeSolapamientoHabilitado(
                any(UUID.class),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(false);

        when(periodoRepository.existeSolapamientoExcluyendoPeriodo(
                any(UUID.class),
                any(UUID.class),
                any(LocalDate.class),
                any(LocalDate.class)
        )).thenReturn(false);
    }

    private PeriodoDisponibilidadRequest crearSolicitud(
            LocalDate inicio,
            LocalDate fin) {

        PeriodoDisponibilidadRequest request =
                new PeriodoDisponibilidadRequest();

        request.setFechaInicio(inicio);
        request.setFechaFin(fin);

        return request;
    }

    private PeriodoDisponibilidad crearPeriodo(boolean habilitado) {
        return new PeriodoDisponibilidad(
                idPeriodo,
                idAlojamiento,
                fechaInicio,
                fechaFin,
                habilitado,
                OffsetDateTime.now()
        );
    }

    private void configurarPropietarioValido() {
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.of(
                        new UsuarioPublicacionInfo(
                                true, true, true, false, true, 0
                        )
                ));

        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(idAnfitrion));
    }

    // REGISTRAR PERIODO

    @Test
    void debeRegistrarPeriodoCorrectamente() {
        when(periodoRepository.save(any(PeriodoDisponibilidad.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PeriodoDisponibilidadResponse resultado =
                service.registrarPeriodo(
                        idAnfitrion, idAlojamiento, solicitud
                );

        assertNotNull(resultado.idPeriodo());
        assertEquals(idAlojamiento, resultado.idAlojamiento());
        assertEquals(fechaInicio, resultado.fechaInicio());
        assertEquals(fechaFin, resultado.fechaFin());
        assertTrue(resultado.habilitado());
        assertNotNull(resultado.fechaCreacion());

        verify(periodoRepository).save(any(PeriodoDisponibilidad.class));
    }

    @Test
    void debeRechazarRegistroConAnfitrionNulo() {
        assertThrows(
                AccessDeniedException.class,
                () -> service.registrarPeriodo(
                        null, idAlojamiento, solicitud
                )
        );

        verifyNoInteractions(periodoRepository);
    }

    @Test
    void debeRechazarRegistroConAlojamientoNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPeriodo(
                        idAnfitrion, null, solicitud
                )
        );

        verifyNoInteractions(usuarioQuery);
    }

    @Test
    void debeRechazarRegistroConSolicitudNula() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPeriodo(
                        idAnfitrion, idAlojamiento, null
                )
        );
    }

    @Test
    void debeRechazarRegistroConFechasNulas() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        crearSolicitud(null, fechaFin)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        crearSolicitud(fechaInicio, null)
                )
        );
    }

    @Test
    void debeRechazarRegistroConRangoDeFechasInvalido() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        crearSolicitud(fechaFin, fechaInicio)
                )
        );
    }

    @Test
    void debeRechazarRegistroCuandoExisteUnaReserva() {
        when(noches.hayNochesOcupadas(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.registrarPeriodo(
                        idAnfitrion, idAlojamiento, solicitud
                )
        );

        verify(periodoRepository, never()).save(any());
    }

    @Test
    void debeRechazarRegistroCuandoExisteSolapamiento() {
        when(periodoRepository.existeSolapamientoHabilitado(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.registrarPeriodo(
                        idAnfitrion, idAlojamiento, solicitud
                )
        );

        verify(periodoRepository, never()).save(any());
    }

    // CONSULTAR PERIODOS

    @Test
    void debeConsultarPeriodosCorrectamente() {
        PeriodoDisponibilidad periodo = crearPeriodo(true);

        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of(periodo));

        List<PeriodoDisponibilidadResponse> resultado =
                service.consultarPeriodos(
                        idAnfitrion, idAlojamiento
                );

        assertEquals(1, resultado.size());
        assertEquals(idPeriodo, resultado.get(0).idPeriodo());
        assertEquals(idAlojamiento, resultado.get(0).idAlojamiento());
        assertEquals(fechaInicio, resultado.get(0).fechaInicio());
        assertEquals(fechaFin, resultado.get(0).fechaFin());
        assertTrue(resultado.get(0).habilitado());
    }

    @Test
    void debeRetornarListaVaciaCuandoNoExistenPeriodos() {
        when(periodoRepository.findByIdAlojamientoOrderByFechaInicioAsc(
                idAlojamiento
        )).thenReturn(List.of());

        List<PeriodoDisponibilidadResponse> resultado =
                service.consultarPeriodos(
                        idAnfitrion, idAlojamiento
                );

        assertTrue(resultado.isEmpty());
    }

    @Test
    void debeRechazarConsultaConAnfitrionNulo() {
        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarPeriodos(
                        null, idAlojamiento
                )
        );

        verifyNoInteractions(periodoRepository);
    }

    @Test
    void debeRechazarConsultaConAlojamientoNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarPeriodos(
                        idAnfitrion, null
                )
        );

        verifyNoInteractions(periodoRepository);
    }

    // ACTUALIZAR PERIODO

    @Test
    void debeActualizarPeriodoCorrectamente() {
        PeriodoDisponibilidad periodo = crearPeriodo(true);

        LocalDate nuevaFechaInicio = fechaInicio.plusDays(1);
        LocalDate nuevaFechaFin = fechaFin.plusDays(2);

        PeriodoDisponibilidadRequest nuevaSolicitud =
                crearSolicitud(nuevaFechaInicio, nuevaFechaFin);

        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.of(periodo));

        when(periodoRepository.save(periodo)).thenReturn(periodo);

        PeriodoDisponibilidadResponse resultado =
                service.actualizarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        idPeriodo,
                        nuevaSolicitud
                );

        assertEquals(nuevaFechaInicio, resultado.fechaInicio());
        assertEquals(nuevaFechaFin, resultado.fechaFin());

        verify(periodoRepository).save(periodo);
    }

    @Test
    void debeRechazarActualizacionConPeriodoNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        null,
                        solicitud
                )
        );

        verify(periodoRepository, never()).save(any());
    }

    @Test
    void debeRechazarActualizacionCuandoElPeriodoNoExiste() {
        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> service.actualizarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        idPeriodo,
                        solicitud
                )
        );
    }

    @Test
    void debeRechazarActualizacionCuandoElPeriodoTieneReservas() {
        PeriodoDisponibilidad periodo = crearPeriodo(true);

        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.of(periodo));

        when(noches.hayNochesOcupadas(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.actualizarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        idPeriodo,
                        solicitud
                )
        );

        verify(periodoRepository, never()).save(any());
    }

    @Test
    void debeRechazarActualizacionCuandoHaySolapamiento() {
        PeriodoDisponibilidad periodo = crearPeriodo(true);

        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.of(periodo));

        when(periodoRepository.existeSolapamientoExcluyendoPeriodo(
                idAlojamiento, idPeriodo, fechaInicio, fechaFin
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.actualizarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        idPeriodo,
                        solicitud
                )
        );

        verify(periodoRepository, never()).save(any());
    }

    @Test
    void debeActualizarPeriodoDeshabilitadoSinValidarSolapamiento() {
        PeriodoDisponibilidad periodo = crearPeriodo(false);

        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.of(periodo));

        when(periodoRepository.save(periodo)).thenReturn(periodo);

        PeriodoDisponibilidadResponse resultado =
                service.actualizarPeriodo(
                        idAnfitrion,
                        idAlojamiento,
                        idPeriodo,
                        solicitud
                );

        assertFalse(resultado.habilitado());

        verify(periodoRepository, never())
                .existeSolapamientoExcluyendoPeriodo(
                        any(), any(), any(), any()
                );
    }

    // DESHABILITAR PERIODO

    @Test
    void debeDeshabilitarPeriodoCorrectamente() {
        PeriodoDisponibilidad periodo = crearPeriodo(true);

        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.of(periodo));

        when(periodoRepository.save(periodo)).thenReturn(periodo);

        PeriodoDisponibilidadResponse resultado =
                service.deshabilitarPeriodo(
                        idAnfitrion, idAlojamiento, idPeriodo
                );

        assertFalse(resultado.habilitado());
        verify(periodoRepository).save(periodo);
    }

    @Test
    void debeRetornarPeriodoCuandoYaEstaDeshabilitado() {
        PeriodoDisponibilidad periodo = crearPeriodo(false);

        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.of(periodo));

        PeriodoDisponibilidadResponse resultado =
                service.deshabilitarPeriodo(
                        idAnfitrion, idAlojamiento, idPeriodo
                );

        assertFalse(resultado.habilitado());
        verify(periodoRepository, never()).save(any());
        verifyNoInteractions(noches);
    }

    @Test
    void debeRechazarDeshabilitacionConPeriodoNulo() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.deshabilitarPeriodo(
                        idAnfitrion, idAlojamiento, null
                )
        );
    }

    @Test
    void debeRechazarDeshabilitacionCuandoElPeriodoNoExiste() {
        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> service.deshabilitarPeriodo(
                        idAnfitrion, idAlojamiento, idPeriodo
                )
        );
    }

    @Test
    void debeRechazarDeshabilitacionCuandoExistenReservas() {
        PeriodoDisponibilidad periodo = crearPeriodo(true);

        when(periodoRepository.findByIdPeriodoAndIdAlojamiento(
                idPeriodo, idAlojamiento
        )).thenReturn(Optional.of(periodo));

        when(noches.hayNochesOcupadas(
                idAlojamiento, fechaInicio, fechaFin
        )).thenReturn(true);

        assertThrows(
                IllegalStateException.class,
                () -> service.deshabilitarPeriodo(
                        idAnfitrion, idAlojamiento, idPeriodo
                )
        );

        verify(periodoRepository, never()).save(any());
    }

    // VALIDACIONES DE PROPIETARIO

    @Test
    void debeRechazarUsuarioQueNoExiste() {
        when(usuarioQuery.consultarParaPublicacion(idAnfitrion))
                .thenReturn(Optional.empty());

        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarPeriodos(
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
                () -> service.consultarPeriodos(
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
                () -> service.consultarPeriodos(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(alojamientoQuery);
    }

    @Test
    void debeRechazarAlojamientoInexistente() {
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> service.consultarPeriodos(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(periodoRepository);
    }

    @Test
    void debeRechazarAnfitrionQueNoEsPropietario() {
        when(alojamientoQuery.consultarIdAnfitrion(idAlojamiento))
                .thenReturn(Optional.of(otroAnfitrion));

        assertThrows(
                AccessDeniedException.class,
                () -> service.consultarPeriodos(
                        idAnfitrion, idAlojamiento
                )
        );

        verifyNoInteractions(periodoRepository);
    }
}
